package fr.skytasul.quests.players;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
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
import fr.skytasul.quests.utils.types.NumberedList;

public class PlayersManagerYAML extends PlayersManager {

	NumberedList<PlayerAccount> loadedAccounts = new NumberedList<>();
	private Map<Integer, String> identifiersIndex = new HashMap<>();
	private int lastAccountID = 0;

	private File directory = new File(BeautyQuests.getInstance().getDataFolder(), "players");

	public synchronized PlayerAccount retrievePlayerAccount(Player p) {
		String identifier = super.getIdentifier(p);
		if (identifiersIndex.containsValue(identifier)) {
			int id = Utils.getKeyByValue(identifiersIndex, identifier);
			PlayerAccount acc = loadedAccounts.get(id);
			if (acc != null) return acc;
			return loadFromFile(identifier, id);
		}

		AbstractAccount absacc = super.createAbstractAccount(p);
		PlayerAccount acc = new PlayerAccount(absacc, lastAccountID + 1);
		addAccount(acc);

		DebugUtils.logMessage("New account registered for " + p.getName() + " (" + acc.abstractAcc.getIdentifier() + "), index " + acc.index + " via " + DebugUtils.stackTraces(2, 4));
		return acc;
	}

	public PlayerQuestDatas createPlayerQuestDatas(PlayerAccount acc, Quest quest) {
		return new PlayerQuestDatas(acc, quest.getID(), 0, false, 0, 0, null, null, null, null, null);
	}

	public void playerQuestDataRemoved(PlayerAccount acc, Quest quest) {}

	public boolean hasAccounts(Player p) {
		return identifiersIndex.containsValue(getIdentifier(p));
	}

	private synchronized PlayerAccount createPlayerAccount(String identifier, int index) {
		AbstractAccount abs = super.createAccountFromIdentifier(identifier);
		if (abs == null) {
			BeautyQuests.logger.info("Player account with identifier " + identifier + " is not enabled, but will be kept in the data file.");
			return new PlayerAccount(new GhostAccount(identifier), index);
		}
		return new PlayerAccount(abs, index);
	}

	void loadAllAccounts() {
		BeautyQuests.getInstance().getLogger().warning("CAUTION - BeautyQuests will now load every single player data into the server's memory. We HIGHLY recommend the server to be restarted at the end of the operation. Be prepared to experiment some lags.");
		for (Entry<Integer, String> entry : identifiersIndex.entrySet()) {
			if (loadedAccounts.contains(entry.getKey())) continue;
			loadFromFile(entry.getValue(), entry.getKey());
		}
	}

	public void debugDuplicate(CommandSender sender) {
		new Thread(() -> {
			try {
				loadAllAccounts();
				int amount = 0;

				Map<String, List<PlayerAccount>> playerAccounts = new HashMap<>();
				for (PlayerAccount acc : loadedAccounts.getOriginalMap().values()) {
					List<PlayerAccount> list = playerAccounts.get(acc.abstractAcc.getIdentifier());
					if (list == null) {
						list = new ArrayList<>();
						playerAccounts.put(acc.abstractAcc.getIdentifier(), list);
					}
					list.add(acc);
				}
				for (Entry<String, List<PlayerAccount>> en : playerAccounts.entrySet()) {
					List<PlayerAccount> list = en.getValue();
					System.out.println("Player occurence : " + list.size() + " accounts");
					int i = 0;
					for (;;) {
						if (i >= list.size() - 1) break;
						PlayerAccount obj = list.get(i);
						PlayerAccount other = list.get(i + 1);
						if (obj.equals(other) && obj.datas.size() <= other.datas.size()) {
							list.remove(i);
							int index = loadedAccounts.indexOf(obj);
							loadedAccounts.remove(index, false);
							identifiersIndex.remove(index);
							removePlayerFile(obj);
							amount++;
						}else i++;
					}
				}
				Thread.sleep(1000);
				
				sender.sendMessage("§e§l§n" + amount + "§r §eduplicated accounts removeds. Total accounts : " + loadedAccounts.valuesSize());
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/*public void debug(Player p) {
		p.sendMessage("Total accounts : " + accounts.valuesSize());
		List<PlayerAccount> ls = playerAccounts.get(p.getUniqueId());
		p.sendMessage("Acutal accounts for your UUID : " + ls.size());
		for (PlayerAccount acc : ls) {
			p.sendMessage(getAccountIndex(acc) + "  =" + acc.abstractAcc.getIdentifier() + " current=" + acc.abstractAcc.isCurrent());
		}
	}*/

	public PlayerAccount getByIndex(String index) { // TODO remove on 0.19
		int id = Utils.parseInt(index);
		PlayerAccount acc = loadedAccounts.get(id);
		if (acc != null) return acc;
		String identifier = identifiersIndex.get(id);
		return loadFromFile(identifier, id);
	}

	private void addAccount(PlayerAccount acc) {
		loadedAccounts.set(acc.index, acc);
		identifiersIndex.put(acc.index, acc.abstractAcc.getIdentifier());
		if (acc.index >= lastAccountID) lastAccountID = acc.index;
	}

	public PlayerAccount loadFromFile(String identifier, int index) {
		File file = new File(directory, identifier + ".yml");
		if (!file.exists()) return null;
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		return loadFromConfig(index, playerConfig);
	}

	private PlayerAccount loadFromConfig(int index, ConfigurationSection datas) {
		PlayerAccount acc = createPlayerAccount((String) datas.get("identifier"), index);
		for (Map<?, ?> questDatas : datas.getMapList("quests")) {
			int questID = (Integer) questDatas.get("questID");
			acc.datas.put(questID,
					new PlayerQuestDatas(acc, questID, Utils.parseLong(questDatas.get("timer")), (Boolean) questDatas.get("finished"), (Integer) questDatas.get("currentBranch"), (Integer) questDatas.get("currentStage"), getStageDatas(questDatas, 0), getStageDatas(questDatas, 1), getStageDatas(questDatas, 2), getStageDatas(questDatas, 3), getStageDatas(questDatas, 4)));
		}
		addAccount(acc);
		return acc;
	}

	public void savePlayerFile(PlayerAccount acc) throws IOException {
		File file = new File(directory, acc.abstractAcc.getIdentifier() + ".yml");
		file.createNewFile();
		YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
		acc.serialize(playerConfig);
		playerConfig.save(file);
	}

	public void removePlayerFile(PlayerAccount acc) {
		File file = new File(directory, acc.abstractAcc.getIdentifier() + ".yml");
		if (file.exists()) file.delete();
	}

	public void load() {
		if (!directory.exists()) directory.mkdirs();

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		if (config.isConfigurationSection("players")) {
			for (String key : config.getConfigurationSection("players").getKeys(false)) {
				try {
					String path = "players." + key;
					int index = Integer.parseInt(key);
					if (config.isConfigurationSection(path)) { // TODO remove on release (beta thing)
						loadFromConfig(index, config.getConfigurationSection(path));
					}else {
						identifiersIndex.put(index, config.getString(path));
					}
				}catch (Exception ex) {
					ex.printStackTrace();
					BeautyQuests.logger.severe("An error occured while loading player account. Data: " + config.get(key));
					continue;
				}
			}
		}
		//DebugUtils.logMessage(accounts.valuesSize() + " accounts loaded for " + playerAccounts.size() + " players.");
	}

	private Map<String, Object> getStageDatas(Map<?, ?> questDatas, int index) {
		return (Map<String, Object>) questDatas.get("stage" + index + "datas");
	}

	public void save() {
		DebugUtils.logMessage("Saving " + loadedAccounts.valuesSize() + " loaded accounts and " + identifiersIndex.size() + " identifiers.");

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		config.set("players", identifiersIndex);

		for (PlayerAccount acc : loadedAccounts) {
			try {
				savePlayerFile(acc);
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
