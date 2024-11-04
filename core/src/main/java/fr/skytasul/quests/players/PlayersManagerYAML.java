package fr.skytasul.quests.players;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.players.accounts.GhostAccount;
import fr.skytasul.quests.utils.QuestUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

public class PlayersManagerYAML extends AbstractPlayersManager {

	private static final int ACCOUNTS_THRESHOLD = 1000;

	private final Cache<Integer, PlayerAccountImplementation> unloadedAccounts = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.MINUTES).build();
	private final Map<Integer, PlayerAccountImplementation> pendingSaveAccounts = new ConcurrentHashMap<>();

	protected final Map<Integer, PlayerAccountImplementation> loadedAccounts = new HashMap<>();
	private final Map<Integer, String> identifiersIndex = new ConcurrentHashMap<>();

	private final File directory = new File(BeautyQuests.getInstance().getDataFolder(), "players");

	private int lastAccountID = 0;

	public File getDirectory() {
		return directory;
	}

	@Override
	public void load(AccountFetchRequest request) {
		String identifier = super.getIdentifier(request.getOfflinePlayer()).orElseThrow(() -> new IllegalArgumentException(
				"Cannot find account for player " + request.getOfflinePlayer().getName()));

		if (identifiersIndex.containsValue(identifier)) {
			int id = Utils.getKeyByValue(identifiersIndex, identifier);
			PlayerAccountImplementation acc;

			// 1. get the account if it's already loaded
			acc = loadedAccounts.get(id);
			if (acc != null) {
				request.loaded(acc, "cached accounts");
			} else {

				// 2. get the account from the "pending unload" list
				acc = request.shouldCache() ? unloadedAccounts.asMap().remove(id) : unloadedAccounts.getIfPresent(id);
				if (acc != null) {
					if (request.shouldCache())
						loadedAccounts.put(id, acc);
					request.loaded(acc, "cached accounts pending unload");
				} else {

					// 3. load the account from the corresponding file
					acc = loadFromFile(id, true);
					if (acc != null) {
						if (request.shouldCache())
							loadedAccounts.put(id, acc);
						request.loaded(acc, "file from index");
					} else {

						// 4. that's pretty bizarre: the account's identifier
						// has an associated index, but no saved datas can be found.
						if (request.mustCreateMissing()) {
							acc = createPlayerAccount(identifier, id);
							if (request.shouldCache())
								addAccount(acc);
							request.created(acc);
						} else {
							request.notLoaded();
						}
					}
				}
			}
		} else if (request.mustCreateMissing()) {
			AbstractAccount absacc = super.newAbstractAccount(request.getOnlinePlayer());
			PlayerAccountImplementation acc = new PlayerAccountImplementation(absacc, lastAccountID + 1);
			if (request.shouldCache())
				addAccount(acc);

			request.created(acc);
		} else {
			request.notLoaded();
		}
	}

	@Override
	protected CompletableFuture<Void> removeAccount(PlayerAccountImplementation acc) {
		loadedAccounts.remove(acc.index);
		identifiersIndex.remove(acc.index);
		return CompletableFuture.runAsync(() -> removePlayerFile(acc.index));
	}

	@Override
	public PlayerQuestDatasImplementation createPlayerQuestDatas(PlayerAccountImplementation acc, Quest quest) {
		return new PlayerQuestDatasImplementation(acc, quest.getId());
	}

	@Override
	public PlayerPoolDatasImplementation createPlayerPoolDatas(PlayerAccountImplementation acc, QuestPool pool) {
		return new PlayerPoolDatasImplementation(acc, pool.getId());
	}

	@Override
	public CompletableFuture<Integer> removeQuestDatas(Quest quest) {
		return CompletableFuture.supplyAsync(() -> {
			loadAllAccounts();
			int amount = 0;

			for (PlayerAccountImplementation account : loadedAccounts.values()) {
				try {
					if (account.removeQuestDatas(quest).get() != null) {
						// we can use the .get() method as the CompletableFuture created by the YAML players manager is
						// already completed
						amount++;
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new CompletionException(e);
				} catch (ExecutionException e) {
					throw new CompletionException(e);
				}
			}

			return amount;
		});
	}

	@Override
	public CompletableFuture<Integer> removePoolDatas(QuestPool pool) {
		return CompletableFuture.supplyAsync(() -> {
			loadAllAccounts();
			int amount = 0;

			for (PlayerAccountImplementation account : loadedAccounts.values()) {
				try {
					if (account.removePoolDatas(pool).get() != null) {
						// we can use the .get() method as the CompletableFuture created by the YAML players manager is
						// already completed
						amount++;
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new CompletionException(e);
				} catch (ExecutionException e) {
					throw new CompletionException(e);
				}
			}

			return amount;
		});
	}

	private synchronized PlayerAccountImplementation createPlayerAccount(String identifier, int index) {
		Validate.notNull(identifier, "Identifier cannot be null (index: " + index + ")");
		var absOpt = super.newAbstractAccount(identifier);
		if (absOpt.isEmpty()) {
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Player account with identifier " + identifier + " is not enabled, but will be kept in the data file.");
			return new PlayerAccountImplementation(new GhostAccount(identifier), index);
		}
		return new PlayerAccountImplementation(absOpt.get(), index);
	}

	void loadAllAccounts() {
		QuestsPlugin.getPlugin().getLoggerExpanded().warning("CAUTION - BeautyQuests will now load every single player data into the server's memory. We HIGHLY recommend the server to be restarted at the end of the operation. Be prepared to experience some lags.");
		for (Entry<Integer, String> entry : identifiersIndex.entrySet()) {
			if (loadedAccounts.containsKey(entry.getKey())) continue;
			try {
				PlayerAccountImplementation acc = loadFromFile(entry.getKey(), false);
				if (acc == null)
					acc = createPlayerAccount(entry.getValue(), entry.getKey());
				addAccount(acc);
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occured when loading player account " + entry.getKey(), ex);
			}
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().info("Total loaded accounts: " + loadedAccounts.size());
	}

	private synchronized void addAccount(PlayerAccountImplementation acc) {
		Validate.notNull(acc);
		loadedAccounts.put(acc.index, acc);
		identifiersIndex.put(acc.index, acc.abstractAcc.getIdentifier());
		if (acc.index >= lastAccountID) lastAccountID = acc.index;
	}

	private PlayerAccountImplementation loadFromFile(int index, boolean msg) {
		File file = new File(directory, index + ".yml");
		if (!file.exists()) return null;
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Loading account #" + index + ". Last file edition: " + new Date(file.lastModified()).toString());
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		return loadFromConfig(index, playerConfig);
	}

	private PlayerAccountImplementation loadFromConfig(int index, ConfigurationSection datas) {
		String identifier = datas.getString("identifier");
		if (identifier == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("No identifier found in file for index " + index + ".");
			identifier = identifiersIndex.get(index);
		}
		PlayerAccountImplementation acc = createPlayerAccount(identifier, index);
		for (Map<?, ?> questConfig : datas.getMapList("quests")) {
			PlayerQuestDatasImplementation questDatas = PlayerQuestDatasImplementation.deserialize(acc, (Map<String, Object>) questConfig);
			acc.questDatas.put(questDatas.questID, questDatas);
		}
		for (Map<?, ?> poolConfig : datas.getMapList("pools")) {
			PlayerPoolDatasImplementation questDatas = PlayerPoolDatasImplementation.deserialize(acc, (Map<String, Object>) poolConfig);
			acc.poolDatas.put(questDatas.getPoolID(), questDatas);
		}
		for (SavableData<?> data : accountDatas) {
			if (datas.contains(data.getId())) {
				acc.additionalDatas.put(data, datas.getObject(data.getId(), data.getDataType()));
			}
		}
		return acc;
	}

	public void savePlayerFile(PlayerAccountImplementation acc) throws IOException {
		File file = new File(directory, acc.index + ".yml");
		file.createNewFile();
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		acc.serialize(playerConfig);
		playerConfig.save(file);
	}

	public void removePlayerFile(int index) {
		File file = new File(directory, index + ".yml");
		if (file.exists()) {
			try {
				Files.delete(file.toPath());
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Removed " + file.getName());
			}catch (IOException e) {
				e.printStackTrace();
			}
		}else QuestsPlugin.getPlugin().getLoggerExpanded().debug("Can't remove " + file.getName() + ": file does not exist");
	}

	@Override
	public void load() {
		super.load();
		if (!directory.exists()) directory.mkdirs();

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		if (config.isConfigurationSection("players")) {
			for (String key : config.getConfigurationSection("players").getKeys(false)) {
				try {
					String path = "players." + key;
					int index = Integer.parseInt(key);
					identifiersIndex.put(index, config.getString(path));
					if (index >= lastAccountID) lastAccountID = index;
				}catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occured while loading player account. Data: " + config.get(key), ex);
				}
			}
		}
		QuestsPlugin.getPlugin().getLoggerExpanded().debug(loadedAccounts.size() + " accounts loaded and " + identifiersIndex.size() + " identifiers.");

		if (identifiersIndex.size() >= ACCOUNTS_THRESHOLD) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(
					"⚠ WARNING - " + identifiersIndex.size() + " players are registered on this server."
					+ " It is recommended to switch to a SQL database setup in order to keep proper performances and scalability."
					+ " In order to do that, setup your database credentials in config.yml (without enabling it) and run the command"
					+ " /quests migrateDatas. Then follow steps on screen.");
		}
	}

	@Override
	public synchronized void save() {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Saving " + loadedAccounts.size() + " loaded accounts and " + identifiersIndex.size() + " identifiers.");

		BeautyQuests.getInstance().getDataFile().set("players", identifiersIndex);

		// as the save can take a few seconds and MAY be done asynchronously,
		// it is possible that the "loadedAccounts" map is being edited concurrently.
		// therefore, we create a new list to avoid this issue.
		Set<PlayerAccountImplementation> accountsToSave = new HashSet<>(loadedAccounts.values());
		accountsToSave.addAll(pendingSaveAccounts.values());
		for (PlayerAccountImplementation acc : accountsToSave) {
			try {
				savePlayerFile(acc);
			}catch (Exception e) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error ocurred while trying to save " + acc.debugName() + " account file", e);
			}
		}
	}

	@Override
	public void unloadAccount(PlayerAccountImplementation acc) {
		loadedAccounts.remove(acc.index);
		unloadedAccounts.put(acc.index, acc);
		pendingSaveAccounts.put(acc.index, acc);
		QuestUtils.runAsync(() -> {
			pendingSaveAccounts.remove(acc.index);
			try {
				savePlayerFile(acc);
			}catch (IOException e) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("An error ocurred while saving player file " + acc.debugName(), e);
			}
		});
	}

}
