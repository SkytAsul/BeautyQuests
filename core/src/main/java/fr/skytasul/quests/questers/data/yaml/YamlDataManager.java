package fr.skytasul.quests.questers.data.yaml;

import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.questers.QuesterManager;
import fr.skytasul.quests.api.questers.data.QuesterData;
import fr.skytasul.quests.api.questers.data.QuesterDataManager;
import fr.skytasul.quests.api.questers.data.QuesterDataManager.QuesterFetchResult.Type;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.players.PlayerManagerImplementation;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class YamlDataManager implements QuesterDataManager {

	protected static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.YamlDataManager");

	private static final int ACCOUNTS_THRESHOLD = 1000;
	private static final String INDEX_FILENAME = "00_index.yml";

	// We map each identifier to an integer because we don't know if the identifier is a valid filename
	private final Map<FullIdentifier, Integer> integerIndex = new ConcurrentHashMap<>();

	/**
	 * Allows to keep track of which yaml file is already loaded, so that we do not end up with 2 quest
	 * data accessing the same file and eventually losing data.
	 */
	private final Map<Integer, YamlQuesterData> cachedData = new HashMap<>();

	private final @NotNull Path dataPath;

	private YamlConfiguration indexConfig;

	public YamlDataManager(@NotNull Path dataPath) {
		this.dataPath = dataPath;
	}

	public @NotNull Path getDataPath() {
		return dataPath;
	}

	private @NotNull Path getIndexFilePath() {
		return dataPath.resolve(INDEX_FILENAME);
	}

	public boolean migrate(@NotNull Path oldDataPath, @NotNull FileConfiguration oldDataFile) throws IOException {
		// TODO remove : migration 2.0
		if (!oldDataFile.isConfigurationSection("players"))
			return false;
		LOGGER.info("Migrating old players data...");

		if (Files.exists(getDataPath())) {
			LOGGER.severe("Cannot migrate old players data because the new format already contains data.");
			return false;
		}

		Files.move(oldDataPath, dataPath);

		var newConfig = new YamlConfiguration();

		for (String key : oldDataFile.getConfigurationSection("players").getKeys(false)) {
			String identifier = oldDataFile.getString("players." + key);
			var section = newConfig.createSection("identifiers." + key);
			section.set("provider", PlayerManagerImplementation.KEY.asString());
			section.set("identifier", identifier);
		}
		oldDataFile.set("players", null);

		newConfig.save(getIndexFilePath().toFile());
		return true;
	}

	@Override
	public void load(@NotNull QuesterManager questerManager) throws DataLoadingException {
		try {
			Files.createDirectories(dataPath);

			if (Files.exists(getIndexFilePath())) {
				indexConfig = YamlConfiguration.loadConfiguration(getIndexFilePath().toFile());

				if (indexConfig.isConfigurationSection("identifiers")) {
					for (String idString : indexConfig.getConfigurationSection("identifiers").getKeys(false)) {
						int id = Integer.parseInt(idString);
						var section = indexConfig.getConfigurationSection("identifiers." + idString);
						integerIndex.put(
								new FullIdentifier(Key.key(section.getString("provider")), section.getString("identifier")),
								id);
					}
				}
			} else {
				indexConfig = new YamlConfiguration();
			}

			LOGGER.debug("{} quester identifiers loaded", integerIndex.size());

			if (integerIndex.size() >= ACCOUNTS_THRESHOLD)
				LOGGER.warning(
						"""
								⚠ WARNING - {} players are registered on this server.
								It is recommended to switch to an SQL database setup in order to keep proper performances and scalability.
								In order to do that, setup your database credentials in config.yml (without enabling it) and run the command
								/quests migrateDatas. Then follow steps on screen.
								""",
						integerIndex.size());
		} catch (IOException ex) {
			throw new DataLoadingException(ex);
		}
	}

	private int getNextIndex() {
		return integerIndex.isEmpty() ? 0 : Collections.max(integerIndex.values()) + 1;
	}

	@Override
	public @NotNull CompletableFuture<QuesterFetchResult> fetchQuester(@NotNull QuesterFetchRequest request) {
		return CompletableFuture.supplyAsync(() -> {
			var fullIdentifier = new FullIdentifier(request.providerKey(), request.identifier());

			int id;
			QuesterFetchResult.Type successType;
			if (integerIndex.containsKey(fullIdentifier)) {
				// quester exists
				id = integerIndex.get(fullIdentifier);
				successType = Type.SUCCESS_LOADED;
			} else if (request.createIfMissing()) {
				// quester does not exist, we create it
				id = getNextIndex();

				integerIndex.put(fullIdentifier, id);
				var dataSection = indexConfig.createSection("identifiers." + id);
				dataSection.set("provider", fullIdentifier.provider().asString());
				dataSection.set("identifier", fullIdentifier.identifier());

				successType = Type.SUCCESS_CREATED;
			} else
				return new QuesterFetchResult(QuesterFetchResult.Type.FAILED_NOT_FOUND, null);

			YamlQuesterData dataHandler;
			if (cachedData.containsKey(id)) {
				dataHandler = cachedData.get(id);
			} else {
				dataHandler = new YamlQuesterData(id, fullIdentifier, this);
				if (request.shouldCache())
					cachedData.put(id, dataHandler);
			}

			return new QuesterFetchResult(successType, dataHandler);
		});
	}

	@Override
	public CompletableFuture<Integer> resetQuestData(int questId) {
		return CompletableFuture.supplyAsync(() -> {
			int amount = 0;
			for (FullIdentifier identifier : integerIndex.keySet()) {
				int dataId = integerIndex.get(identifier);
				try {
					if (cachedData.containsKey(dataId)) {
						if (cachedData.get(dataId).removeQuestDataSilently(questId) != null)
							amount++;
					} else {
						var data = new YamlQuesterData(dataId, identifier, this);
						if (data.removeQuestDataSilently(questId) != null) {
							amount++;
							data.save();
						}
					}
				} catch (DataSavingException ex) {
					LOGGER.severe("Failed to reset quest {} data for {}", ex, questId, identifier);
				}
			}
			return amount;
		});
	}

	@Override
	public CompletableFuture<Integer> resetPoolData(int poolId) {
		return CompletableFuture.supplyAsync(() -> {
			int amount = 0;
			for (FullIdentifier identifier : integerIndex.keySet()) {
				int dataId = integerIndex.get(identifier);
				try {
					if (cachedData.containsKey(dataId)) {
						if (cachedData.get(dataId).removePoolDataSilently(poolId) != null)
							amount++;
					} else {
						var data = new YamlQuesterData(dataId, identifier, this);
						if (data.removePoolDataSilently(poolId) != null) {
							amount++;
							data.save();
						}
					}
				} catch (DataSavingException ex) {
					LOGGER.severe("Failed to reset pool {} data for {}", ex, poolId, identifier);
				}
			}
			return amount;
		});
	}

	@Override
	public @NotNull Iterator<? extends QuesterData> getAll() {
		return integerIndex.entrySet().stream().map(entry -> {
			YamlQuesterData data = cachedData.get(entry.getValue());
			if (data == null) {
				data = new YamlQuesterData(entry.getValue(), entry.getKey(), this);
			}

			return data;
		}).iterator();
	}

	@Override
	public void save() throws DataSavingException {
		try {
			indexConfig.save(getIndexFilePath().toFile());
		} catch (IOException ex) {
			throw new DataSavingException("Failed to save index file", ex);
		}
	}

	@Override
	public void unload() {
		// nothing to do: the files are never kept open
		cachedData.clear();
	}

	protected void uncache(@NotNull YamlQuesterData data) {
		cachedData.remove(data.getId());
	}

	protected void remove(@NotNull YamlQuesterData data) {
		cachedData.remove(data.getId());
		integerIndex.remove(data.getFullIdentifier());
		indexConfig.set("identifiers." + data.getId(), null);
	}

	record FullIdentifier(@NotNull Key provider, @NotNull String identifier) {
		@Override
		public final String toString() {
			return provider.asString() + '|' + identifier;
		}
	}

}
