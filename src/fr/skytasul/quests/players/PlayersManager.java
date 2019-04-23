package fr.skytasul.quests.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Accounts;
import fr.skytasul.quests.utils.types.NumberedList;
import net.citizensnpcs.npc.ai.NPCHolder;

public class PlayersManager {
	
	private static NumberedList<PlayerAccount> accounts = new NumberedList<>();
	private static Map<UUID, List<PlayerAccount>> playerAccounts = /*Collections.synchronizedMap(*/new HashMap<>();
	
	public synchronized static PlayerAccount getPlayerAccount(Player p){
		if (p instanceof NPCHolder) return null;
		UUID id = p.getUniqueId();
		List<PlayerAccount> playerList = getPlayerAccounts(id);
		
		for (PlayerAccount acc : playerList){
			if (acc.abstractAcc.isCurrent()) return acc;
		}
		synchronized (accounts) {
			AbstractAccount absacc = createAbstractAccount(p);
			PlayerAccount acc = new PlayerAccount(absacc);
			playerList.add(acc);
			int index = accounts.add(acc);
			
			DebugUtils.logMessage("New account registered for " + p.getName() + " (" + acc.abstractAcc.getIdentifier() + "), index " + index + " via " + DebugUtils.stackTraces(2, 4));
			return acc;
		}
	}
	
	public static int getAccountIndex(PlayerAccount account){
		return accounts.indexOf(account);
	}
	
	public static PlayerAccount getByIndex(String index){
		try{
			return accounts.get(Integer.parseInt(index));
		}catch (IndexOutOfBoundsException ex){
			return null;
		}
	}
	
	private synchronized static PlayerAccount createPlayerAccount(String identifier){
		AbstractAccount abs = null;
		if (identifier.startsWith("Hooked|")){
			if (!QuestsConfiguration.hookAccounts()) throw new MissingDependencyException("AccountsHook is not enabled or config parameter is disabled, but saved datas need it.");
			String nidentifier = identifier.substring(7);
			try{
				abs = Accounts.getAccountFromIdentifier(nidentifier);
			}catch (Throwable ex){
				ex.printStackTrace();
			}
		}else if (QuestsConfiguration.hookAccounts()){
			abs = Accounts.createAccountFromUUID(UUID.fromString(identifier));
		}else abs = new UUIDAccount(UUID.fromString(identifier));
		
		PlayerAccount acc = new PlayerAccount(abs);
		UUID id = acc.abstractAcc.getOfflinePlayer().getUniqueId();
		getPlayerAccounts(id).add(acc);
		return acc;
	}
	
	private static AbstractAccount createAbstractAccount(Player p){
		return QuestsConfiguration.hookAccounts() ? Accounts.getPlayerAccount(p) : new UUIDAccount(p.getUniqueId());
	}
	
	public static void debugDuplicate(CommandSender sender){
		new Thread(() -> {
				try {
					int amount = 0;
					for (Entry<UUID, List<PlayerAccount>> en : playerAccounts.entrySet()){
						List<PlayerAccount> list = en.getValue();
						System.out.println("Player occurence : " + list.size() + " accounts");
						int i = 0;
						for (;;){
							if (i >= list.size() - 1) break;
							PlayerAccount obj = list.get(i);
							if (obj.equals(list.get(i+1))){
								list.remove(i);
								accounts.remove(obj, false);
								amount++;
							}else i++;
						}
					}
					Thread.sleep(1000);

					NumberedList<PlayerAccount> newAccounts = new NumberedList<>();
					for (PlayerAccount acc : accounts){
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
	public synchronized static List<PlayerAccount> getPlayerAccounts(UUID id){
		List<PlayerAccount> ls;
		while((ls = playerAccounts.get(id)) == null) playerAccounts.put(id, /*Collections.synchronizedList(*/new ArrayList<>(3));
		return ls;
	}
	
	
	public static void debug(Player p){
		p.sendMessage("Total accounts : " + accounts.valuesSize());
		List<PlayerAccount> ls = playerAccounts.get(p.getUniqueId());
		p.sendMessage("Acutal accounts for your UUID : " + ls.size());
		for (PlayerAccount acc : ls){
			p.sendMessage(getAccountIndex(acc) + "  =" + acc.abstractAcc.getIdentifier() + " current=" + acc.abstractAcc.isCurrent());
		}
	}
	

	public static void load(FileConfiguration config){
		if (!config.isConfigurationSection("players")) return;
		accounts.clear();
		playerAccounts.clear();
		for (Entry<String, Object> en : config.getConfigurationSection("players").getValues(false).entrySet()){
			try{
				accounts.set(Integer.parseInt(en.getKey()), createPlayerAccount((String) en.getValue()));
			}catch (Throwable ex){
				ex.printStackTrace();
				BeautyQuests.logger.severe("An error occured while loading player account. Identifier: " + en.getValue());
				continue;
			}
		}
		DebugUtils.logMessage(accounts.valuesSize() + " accounts loaded for " + playerAccounts.size() + " players.");
	}
	
	public static void save(FileConfiguration config){
		DebugUtils.logMessage("Saving " + accounts.valuesSize() + " accounts for " + playerAccounts.size() + " players.");
		Map<Integer, String> list = new HashMap<>();
		for (Entry<Integer, PlayerAccount> en : accounts.getOriginalMap().entrySet()){
			list.put(en.getKey(), en.getValue().abstractAcc.getIdentifier());
		}
		config.set("players", list);
	}
	
}
