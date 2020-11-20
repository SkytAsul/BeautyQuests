package fr.skytasul.quests.players;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.players.accounts.GhostAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Utils;

public class PlayersManagerYAML extends PlayersManager {

	Map<Integer, PlayerAccount> loadedAccounts = new HashMap<>();
	private Map<Integer, String> identifiersIndex = new HashMap<>();
	private int lastAccountID = 0;

	private File directory = new File(BeautyQuests.getInstance().getDataFolder(), "players");
	
	@Override
	protected Entry<PlayerAccount, Boolean> load(Player player) {
		String identifier = super.getIdentifier(player);
		if (identifiersIndex.containsValue(identifier)) {
			int id = Utils.getKeyByValue(identifiersIndex, identifier);
			return new AbstractMap.SimpleEntry<>(getByIndex(id), false);
		}

		AbstractAccount absacc = super.createAbstractAccount(player);
		PlayerAccount acc = new PlayerAccount(absacc, lastAccountID + 1);
		addAccount(acc);

		return new AbstractMap.SimpleEntry<>(acc, true);
	}

	public PlayerQuestDatas createPlayerQuestDatas(PlayerAccount acc, Quest quest) {
		return new PlayerQuestDatas(acc, quest.getID());
	}

	public void playerQuestDataRemoved(PlayerAccount acc, Quest quest, PlayerQuestDatas datas) {}

	public int removeQuestDatas(Quest quest) {
		loadAllAccounts();
		int amount = 0;
		
		for (PlayerAccount account : loadedAccounts.values()) {
			if (account.removeQuestDatas(quest) != null) amount++;
		}
		
		return amount;
	}

	public boolean hasAccounts(Player p) {
		return identifiersIndex.containsValue(getIdentifier(p));
	}

	private synchronized PlayerAccount createPlayerAccount(String identifier, int index) {
		Validate.notNull(identifier, "Identifier cannot be null (index: " + index + ")");
		AbstractAccount abs = super.createAccountFromIdentifier(identifier);
		if (abs == null) {
			BeautyQuests.logger.info("Player account with identifier " + identifier + " is not enabled, but will be kept in the data file.");
			return new PlayerAccount(new GhostAccount(identifier), index);
		}
		return new PlayerAccount(abs, index);
	}

	void loadAllAccounts() {
		BeautyQuests.getInstance().getLogger().warning("CAUTION - BeautyQuests will now load every single player data into the server's memory. We HIGHLY recommend the server to be restarted at the end of the operation. Be prepared to experience some lags.");
		for (Entry<Integer, String> entry : identifiersIndex.entrySet()) {
			if (loadedAccounts.containsKey(entry.getKey())) continue;
			try {
				PlayerAccount acc = loadFromFile(entry.getKey());
				if (acc == null) {
					acc = createPlayerAccount(entry.getValue(), entry.getKey());
					addAccount(acc);
				}
			}catch (Exception ex) {
				BeautyQuests.getInstance().getLogger().severe("An error occured when loading player account " + entry.getKey());
				ex.printStackTrace();
				continue;
			}
		}
		BeautyQuests.getInstance().getLogger().info("Total loaded accounts: " + loadedAccounts.size());
	}

	public void debugDuplicate() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.kickPlayer("§cCleanup operation.");
		}
		PlayersManager.cachedAccounts.clear();

		loadAllAccounts();
		int amount = 0;

		Map<String, List<PlayerAccount>> playerAccounts = new HashMap<>();
		for (PlayerAccount acc : loadedAccounts.values()) {
			List<PlayerAccount> list = playerAccounts.get(acc.abstractAcc.getIdentifier());
			if (list == null) {
				list = new ArrayList<>();
				playerAccounts.put(acc.abstractAcc.getIdentifier(), list);
			}
			list.add(acc);
		}
		BeautyQuests.getInstance().getLogger().info(playerAccounts.size() + " unique identifiers.");

		List<String> removed = new ArrayList<>();
		for (Entry<String, List<PlayerAccount>> en : playerAccounts.entrySet()) {
			if (removed.contains(en.getKey())) System.out.println("CRITICAL - Already removed " + en.getKey());

			List<PlayerAccount> list = en.getValue();

			int maxID = 0;
			int maxSize = 0;
			for (int i = 0; i < list.size(); i++) {
				PlayerAccount acc = list.get(i);
				if (acc.datas.size() > maxSize) {
					maxID = i;
					maxSize = acc.datas.size();
				}
			}
			for (int i = 0; i < list.size(); i++) {
				if (i != maxID) {
					PlayerAccount acc = list.get(i);
					int index = Utils.getKeyByValue(loadedAccounts, acc);
					loadedAccounts.remove(index);
					identifiersIndex.remove(index);
					removePlayerFile(index);
					amount++;
				}
			}
			removed.add(en.getKey());
		}

		BeautyQuests.getInstance().getLogger().info(amount + " duplicated accounts removeds. Total loaded accounts/identifiers: " + loadedAccounts.size() + "/" + identifiersIndex.size());
		BeautyQuests.getInstance().getLogger().info("Now scanning for remaining duplicated accounts...");
		boolean dup = false;
		for (String id : identifiersIndex.values()) {
			int size = Utils.getKeysByValue(identifiersIndex, id).size();
			if (size != 1) {
				dup = true;
				System.out.println(size + " accounts with identifier " + id);
			}
		}
		if (dup) BeautyQuests.getInstance().getLogger().warning("There is still duplicated accounts.");
		BeautyQuests.getInstance().getLogger().info("Operation complete.");
	}

	public PlayerAccount getByIndex(Object index) { // TODO remove on 0.19
		int id = index instanceof Integer ? (int) index : Utils.parseInt(index);
		PlayerAccount acc = loadedAccounts.get(id);
		if (acc != null) return acc;
		acc = loadFromFile(id);
		if (acc != null) return acc;
		acc = createPlayerAccount(identifiersIndex.get(id), id);
		addAccount(acc);
		return acc;
	}

	private void addAccount(PlayerAccount acc) {
		loadedAccounts.put(acc.index, acc);
		identifiersIndex.put(acc.index, acc.abstractAcc.getIdentifier());
		if (acc.index >= lastAccountID) lastAccountID = acc.index;
	}

	public PlayerAccount loadFromFile(int index) {
		File file = new File(directory, index + ".yml");
		if (!file.exists()) return null;
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		return loadFromConfig(index, playerConfig);
	}

	private PlayerAccount loadFromConfig(int index, ConfigurationSection datas) {
		String identifier = datas.getString("identifier");
		if (identifier == null) {
			BeautyQuests.logger.warning("No identifier found in file for index " + index + ".");
			identifier = identifiersIndex.get(index);
		}
		PlayerAccount acc = createPlayerAccount(identifier, index);
		for (Map<?, ?> questConfig : datas.getMapList("quests")) {
			PlayerQuestDatas questDatas = PlayerQuestDatas.deserialize(acc, (Map<String, Object>) questConfig);
			acc.datas.put(questDatas.questID, questDatas);
		}
		addAccount(acc);
		return acc;
	}

	public void savePlayerFile(PlayerAccount acc) throws IOException {
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
				DebugUtils.logMessage("Removed " + file.getName());
			}catch (IOException e) {
				e.printStackTrace();
			}
		}else DebugUtils.logMessage("Can't remove " + file.getName() + ": file does not exist");
	}

	public void load() {
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
					ex.printStackTrace();
					BeautyQuests.logger.severe("An error occured while loading player account. Data: " + config.get(key));
					continue;
				}
			}
		}
		DebugUtils.logMessage(loadedAccounts.size() + " accounts loaded and " + identifiersIndex.size() + " identifiers.");
	}

	public void save() {
		DebugUtils.logMessage("Saving " + loadedAccounts.size() + " loaded accounts and " + identifiersIndex.size() + " identifiers.");

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		config.set("players", identifiersIndex);

		for (PlayerAccount acc : loadedAccounts.values()) {
			try {
				savePlayerFile(acc);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
