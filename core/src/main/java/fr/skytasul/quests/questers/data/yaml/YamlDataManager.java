package fr.skytasul.quests.questers.data.yaml;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.questers.data.DataSavingException;
import fr.skytasul.quests.questers.data.QuesterDataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class YamlDataManager implements QuesterDataManager {

	private static final int ACCOUNTS_THRESHOLD = 1000;

	// We map each identifier to an integer because we don't know if the identifier is a valid filename
	private final Map<String, Integer> fullIdentifiersIndex = new ConcurrentHashMap<>();

	private final Path dataPath = QuestsPlugin.getPlugin().getDataFolder().toPath().resolve("players");

	public void load() throws IOException {
		Files.createDirectories(dataPath);

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		if (config.isConfigurationSection("players")) {
			// TODO remove : migration 2.0
			for (String key : config.getConfigurationSection("players").getKeys(false)) {
				String path = "players." + key;
				int index = Integer.parseInt(key);
				fullIdentifiersIndex.put(config.getString(path), index);
			}
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("{} quester identifiers loaded", fullIdentifiersIndex.size());

		if (fullIdentifiersIndex.size() >= ACCOUNTS_THRESHOLD)
			QuestsPlugin.getPlugin().getLoggerExpanded().warningArgs(
					"""
							⚠ WARNING - {} players are registered on this server.
							It is recommended to switch to a SQL database setup in order to keep proper performances and scalability.
							In order to do that, setup your database credentials in config.yml (without enabling it) and run the command
							/quests migrateDatas. Then follow steps on screen.
							""",
					fullIdentifiersIndex.size());
	}

	private int getNextIndex() {
		return Collections.max(fullIdentifiersIndex.values()) + 1;
	}

	@Override
	public @NotNull CompletableFuture<QuesterFetchResult> loadQuester(@NotNull QuesterFetchRequest request) {
		var future = CompletableFuture.supplyAsync(() -> {
			String fullIdentifier = request.providerKey().asString() + "|" + request.identifier();

			if (fullIdentifiersIndex.containsKey(fullIdentifier)) {
				// quester exists
				int id = fullIdentifiersIndex.get(fullIdentifier);
				var dataHandler = new YamlDataHandler(dataPath.resolve(id + ".yml"));

				return new QuesterFetchResult(QuesterFetchResult.Type.SUCCESS_LOADED, dataHandler);
			} else if (request.createIfMissing()) {
				// quester does not exist, we create it
				int id = getNextIndex();
				fullIdentifiersIndex.put(fullIdentifier, id);
				var dataHandler = new YamlDataHandler(dataPath.resolve(id + ".yml"));

				return new QuesterFetchResult(QuesterFetchResult.Type.SUCCESS_CREATED, dataHandler);
			} else
				return new QuesterFetchResult(QuesterFetchResult.Type.FAILED_NOT_FOUND, null);
		});

		return future;
	}

	@Override
	public void save() throws DataSavingException {
		BeautyQuests.getInstance().getDataFile().set("players",
				fullIdentifiersIndex.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey)));
	}

	@Override
	public void unload() {}

}
