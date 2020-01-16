package fr.skytasul.quests.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.players.accounts.GhostAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.NumberedList;

public class PlayersManagerYAML extends PlayersManager {

	NumberedList<PlayerAccount> accounts = new NumberedList<>();
	private Map<UUID, List<PlayerAccount>> playerAccounts = new HashMap<>();

	public synchronized PlayerAccount retrievePlayerAccount(Player p) {
		UUID id = p.getUniqueId();
		List<PlayerAccount> playerList = getPlayerAccounts(id);

		for (PlayerAccount acc : playerList) {
			if (acc.abstractAcc.isCurrent()) return acc;
		}
		synchronized (accounts) {
			AbstractAccount absacc = super.createAbstractAccount(p);
			PlayerAccount acc = new PlayerAccount(absacc, -1);
			playerList.add(acc);
			acc.index = accounts.add(acc);

			DebugUtils.logMessage("New account registered for " + p.getName() + " (" + acc.abstractAcc.getIdentifier() + "), index " + acc.index + " via " + DebugUtils.stackTraces(2, 4));
			return acc;
		}
	}

	public PlayerQuestDatas createPlayerQuestDatas(PlayerAccount acc, Quest quest) {
		return new PlayerQuestDatas(acc, quest.getID(), 0, false, 0, 0, null, null, null, null, null);
	}

	public void playerQuestDataRemoved(PlayerAccount acc, Quest quest) {}

	public boolean hasAccounts(Player p) {
		return (playerAccounts.containsKey(p.getUniqueId()));
	}

	private synchronized PlayerAccount createPlayerAccount(String identifier, int index) {
		AbstractAccount abs = super.createAccountFromIdentifier(identifier);
		if (abs == null) {
			BeautyQuests.logger.info("Player account with identifier " + identifier + " is not enabled, but will be kept in the data file.");
			return new PlayerAccount(new GhostAccount(identifier), index);
		}
		PlayerAccount acc = new PlayerAccount(abs, index);
		UUID id = abs.getOfflinePlayer().getUniqueId();
		getPlayerAccounts(id).add(acc);
		return acc;
	}

	public void debugDuplicate(CommandSender sender) {
		new Thread(() -> {
			try {
				int amount = 0;
				for (Entry<UUID, List<PlayerAccount>> en : playerAccounts.entrySet()) {
					List<PlayerAccount> list = en.getValue();
					System.out.println("Player occurence : " + list.size() + " accounts");
					int i = 0;
					for (;;) {
						if (i >= list.size() - 1) break;
						PlayerAccount obj = list.get(i);
						if (obj.equals(list.get(i + 1))) {
							list.remove(i);
							accounts.remove(obj, false);
							amount++;
						}else i++;
					}
				}
				Thread.sleep(1000);

				NumberedList<PlayerAccount> newAccounts = new NumberedList<>();
				for (PlayerAccount acc : accounts) {
					newAccounts.add(acc);
				}
				accounts = newAccounts;
				sender.sendMessage("§e§l§n" + amount + "§r §eduplicated accounts removeds. Total accounts : " + accounts.valuesSize());
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Get all registered accounts for player
	 * @param id Player's UUID
	 * @return the list of all accounts (<b>WARNING</B> it's the real instance !)
	 */
	public synchronized List<PlayerAccount> getPlayerAccounts(UUID id) {
		List<PlayerAccount> ls;
		while ((ls = playerAccounts.get(id)) == null) playerAccounts.put(id, new ArrayList<>(5));
		return ls;
	}

	/*public void debug(Player p) {
		p.sendMessage("Total accounts : " + accounts.valuesSize());
		List<PlayerAccount> ls = playerAccounts.get(p.getUniqueId());
		p.sendMessage("Acutal accounts for your UUID : " + ls.size());
		for (PlayerAccount acc : ls) {
			p.sendMessage(getAccountIndex(acc) + "  =" + acc.abstractAcc.getIdentifier() + " current=" + acc.abstractAcc.isCurrent());
		}
	}*/

	public PlayerAccount getByIndex(String index) {
		return accounts.get(Utils.parseInt(index));
	}


	public void load() {
		accounts.clear();
		playerAccounts.clear();

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		if (config.isConfigurationSection("players")) {
			for (String key : config.getConfigurationSection("players").getKeys(false)) {
				try {
					String path = "players." + key;
					int index = Integer.parseInt(key);
					PlayerAccount acc;
					if (config.isConfigurationSection(path)) {
						ConfigurationSection datas = config.getConfigurationSection(path);
						acc = createPlayerAccount((String) datas.get("identifier"), index);
						for (Map<?, ?> questDatas : datas.getMapList("quests")) {
							int questID = (Integer) questDatas.get("questID");
							acc.datas.put(questID,
									new PlayerQuestDatas(acc, questID, Utils.parseLong(questDatas.get("timer")), (Boolean) questDatas.get("finished"), (Integer) questDatas.get("currentBranch"), (Integer) questDatas.get("currentStage"), getStageDatas(questDatas, 0), getStageDatas(questDatas, 1), getStageDatas(questDatas, 2), getStageDatas(questDatas, 3), getStageDatas(questDatas, 4)));
						}
					}else {
						acc = createPlayerAccount(config.getString(path), index);
					}
					accounts.set(index, acc);
				}catch (Exception ex) {
					ex.printStackTrace();
					BeautyQuests.logger.severe("An error occured while loading player account. Data: " + config.get(key));
					continue;
				}
			}
		}
		DebugUtils.logMessage(accounts.valuesSize() + " accounts loaded for " + playerAccounts.size() + " players.");
	}

	private Map<String, Object> getStageDatas(Map<?, ?> questDatas, int index) {
		return (Map<String, Object>) questDatas.get("stage" + index + "datas");
	}

	public void save() {
		DebugUtils.logMessage("Saving " + accounts.valuesSize() + " accounts for " + playerAccounts.size() + " players.");

		FileConfiguration config = BeautyQuests.getInstance().getDataFile();
		Map<Integer, Map<String, Object>> list = new HashMap<>();
		for (Entry<Integer, PlayerAccount> en : accounts.getOriginalMap().entrySet()) {
			list.put(en.getKey(), en.getValue().serialize());
		}
		config.set("players", list);
	}

}
