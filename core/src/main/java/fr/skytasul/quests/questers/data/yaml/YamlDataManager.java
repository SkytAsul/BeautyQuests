package fr.skytasul.quests.questers.data.yaml;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.questers.data.QuesterDataManager;
import fr.skytasul.quests.questers.data.QuesterDataManager.QuesterFetchResult.Type;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class YamlDataManager implements QuesterDataManager {

	private static final int ACCOUNTS_THRESHOLD = 1000;

	// We map each identifier to an integer because we don't know if the identifier is a valid filename
	private final Map<String, Integer> fullIdentifiersIndex = new ConcurrentHashMap<>();

	/**
	 * Allows to keep track of which yaml file is already loaded, so that we do not end up with 2 quest
	 * data accessing the same file and eventually losing data.
	 */
	private final Map<Integer, YamlQuesterData> cachedData = new HashMap<>();

	protected final Path dataPath = QuestsPlugin.getPlugin().getDataFolder().toPath().resolve("players");

	@Override
	public void load() throws DataLoadingException {
		try {
			Files.createDirectories(dataPath);

			FileConfiguration config = BeautyQuests.getInstance().getDataFile();
			if (config.isConfigurationSection("players")) {
				// TODO remove : migration 2.0
				for (String key : config.getConfigurationSection("players").getKeys(false)) {
					int index = Integer.parseInt(key);
					String identifier = config.getString("players." + key);
					config.set("identifiers." + identifier, index);
				}
				config.set("players", null);
			}

			if (config.isConfigurationSection("identifiers"))
				for (String key : config.getConfigurationSection("identifiers").getKeys(false))
					fullIdentifiersIndex.put(key, config.getInt("identifiers." + key));

			QuestsPlugin.getPlugin().getLoggerExpanded().debug("{} quester identifiers loaded", fullIdentifiersIndex.size());

			if (fullIdentifiersIndex.size() >= ACCOUNTS_THRESHOLD)
				QuestsPlugin.getPlugin().getLoggerExpanded().warningArgs(
						"""
								⚠ WARNING - {} players are registered on this server.
								It is recommended to switch to an SQL database setup in order to keep proper performances and scalability.
								In order to do that, setup your database credentials in config.yml (without enabling it) and run the command
								/quests migrateDatas. Then follow steps on screen.
								""",
						fullIdentifiersIndex.size());
		} catch (IOException ex) {
			throw new DataLoadingException(ex);
		}
	}

	private int getNextIndex() {
		return Collections.max(fullIdentifiersIndex.values()) + 1;
	}

	@Override
	public @NotNull CompletableFuture<QuesterFetchResult> loadQuester(@NotNull QuesterFetchRequest request) {
		return CompletableFuture.supplyAsync(() -> {
			String fullIdentifier = request.providerKey().asString() + "|" + request.identifier();

			int id;
			QuesterFetchResult.Type successType;
			if (fullIdentifiersIndex.containsKey(fullIdentifier)) {
				// quester exists
				id = fullIdentifiersIndex.get(fullIdentifier);
				successType = Type.SUCCESS_LOADED;
			} else if (request.createIfMissing()) {
				// quester does not exist, we create it
				id = getNextIndex();
				fullIdentifiersIndex.put(fullIdentifier, id);
				successType = Type.SUCCESS_CREATED;
			} else
				return new QuesterFetchResult(QuesterFetchResult.Type.FAILED_NOT_FOUND, null);

			YamlQuesterData dataHandler;
			if (cachedData.containsKey(id)) {
				dataHandler = cachedData.get(id);
			} else {
				dataHandler = new YamlQuesterData(id, this);
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
			for (String identifier : fullIdentifiersIndex.keySet()) {
				int dataId = fullIdentifiersIndex.get(identifier);
				try {
					if (cachedData.containsKey(dataId)) {
						if (cachedData.get(dataId).removeQuestDataSilently(questId) != null)
							amount++;
					} else {
						var data = new YamlQuesterData(questId, this);
						if (data.removeQuestDataSilently(questId) != null) {
							amount++;
							data.save();
						}
					}
				} catch (DataSavingException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Failed to reset quest {} data for {}", ex, questId,
							identifier);
				}
			}
			return amount;
		});
	}

	@Override
	public CompletableFuture<Integer> resetPoolData(int poolId) {
		// TODO
		return null;
	}

	@Override
	public void save() throws DataSavingException {
		BeautyQuests.getInstance().getDataFile().createSection("identifiers", fullIdentifiersIndex);
	}

	@Override
	public void unload() {
		// nothing to do: the files are never kept open
		cachedData.clear();
	}

	protected void uncache(YamlQuesterData data) {
		cachedData.remove(data.getId());
	}

}
