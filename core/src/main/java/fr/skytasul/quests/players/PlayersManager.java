package fr.skytasul.quests.players;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.players.accounts.UUIDAccount;
import fr.skytasul.quests.players.events.PlayerAccountJoinEvent;
import fr.skytasul.quests.players.events.PlayerAccountLeaveEvent;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.compatibility.Accounts;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;
import net.citizensnpcs.npc.ai.NPCHolder;

public abstract class PlayersManager {

	public static PlayersManager manager;

	//protected abstract PlayerAccount retrievePlayerAccount(Player p);

	protected abstract Entry<PlayerAccount, Boolean> load(Player player);
	
	public abstract PlayerQuestDatas createPlayerQuestDatas(PlayerAccount acc, Quest quest);

	public abstract void playerQuestDataRemoved(PlayerAccount acc, Quest quest, PlayerQuestDatas datas);

	public abstract PlayerPoolDatas createPlayerPoolDatas(PlayerAccount acc, QuestPool pool);
	
	public abstract int removeQuestDatas(Quest quest);
	
	//public abstract boolean hasAccounts(Player p);

	public abstract void load();

	public abstract void save();

	public AbstractAccount createAbstractAccount(Player p) {
		return QuestsConfiguration.hookAccounts() ? Accounts.getPlayerAccount(p) : new UUIDAccount(p.getUniqueId());
	}

	public String getIdentifier(Player p) {
		return QuestsConfiguration.hookAccounts() ? "Hooked|" + Accounts.getPlayerCurrentIdentifier(p) : p.getUniqueId().toString();
	}

	protected AbstractAccount createAccountFromIdentifier(String identifier) {
		if (identifier.startsWith("Hooked|")){
			if (!QuestsConfiguration.hookAccounts()) throw new MissingDependencyException("AccountsHook is not enabled or config parameter is disabled, but saved datas need it.");
			String nidentifier = identifier.substring(7);
			try{
				return Accounts.getAccountFromIdentifier(nidentifier);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}else {
			try{
				UUID uuid = UUID.fromString(identifier);
				if (QuestsConfiguration.hookAccounts()){
					try{
						return Accounts.createAccountFromUUID(uuid);
					}catch (UnsupportedOperationException ex){
						BeautyQuests.logger.warning("Can't migrate an UUID account to a hooked one.");
					}
				}else return new UUIDAccount(uuid);
			}catch (IllegalArgumentException ex){
				BeautyQuests.logger.warning("Account identifier " + identifier + " is not valid.");
			}
		}
		return null;
	}

	protected static Map<Player, PlayerAccount> cachedAccounts = new HashMap<>();
	
	public synchronized static void loadPlayer(Player p) {
		DebugUtils.logMessage("Loading player " + p.getName() + "...");
		cachedAccounts.remove(p);
		Bukkit.getScheduler().runTaskAsynchronously(BeautyQuests.getInstance(), () -> {
			int i = 2;
			while (i > 0) {
				i--;
				try {
					Entry<PlayerAccount, Boolean> entry = manager.load(p);
					PlayerAccount account = entry.getKey();
					boolean created = entry.getValue();
					if (created) DebugUtils.logMessage("New account registered for " + p.getName() + " (" + account.abstractAcc.getIdentifier() + "), index " + account.index + " via " + DebugUtils.stackTraces(2, 4));
					if (!p.isOnline()) return;
					cachedAccounts.put(p, account);
					Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), () -> {
						if (p.isOnline()) {
							Bukkit.getPluginManager().callEvent(new PlayerAccountJoinEvent(p, account, created));
						}else BeautyQuests.logger.warning("Player " + p.getName() + " has quit the server while loading its datas. This may be a bug.");
					});
					return;
				}catch (Exception ex) {
					ex.printStackTrace();
					BeautyQuests.logger.severe("An error ocurred while trying to load datas of " + p.getName() + ". Doing " + i + " more attempt.");
				}
			}
			BeautyQuests.logger.severe("Datas of " + p.getName() + " have failed to load. This may cause MANY issues.");
		});
	}
	
	public synchronized static void unloadPlayer(Player p) {
		PlayerAccount acc = cachedAccounts.get(p);
		if (acc == null) return;
		Bukkit.getPluginManager().callEvent(new PlayerAccountLeaveEvent(p, acc));
		cachedAccounts.remove(p);
	}
	
	public static PlayerAccount getPlayerAccount(Player p) {
		if (p instanceof NPCHolder) return null;
		
		PlayerAccount account = cachedAccounts.get(p);
		/*if (account == null || !account.isCurrent()) {
			account = manager.retrievePlayerAccount(p);
			cachedAccounts.put(p, account);
		}*/
		return account;
	}
	
}
