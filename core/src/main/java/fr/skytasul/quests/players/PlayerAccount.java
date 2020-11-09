package fr.skytasul.quests.players;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Utils;

public class PlayerAccount {

	public final AbstractAccount abstractAcc;
	protected final Map<Integer, PlayerQuestDatas> questDatas = new HashMap<>();
	protected final Map<Integer, PlayerPoolDatas> poolDatas = new HashMap<>();
	protected final int index;
	
	public PlayerAccount(AbstractAccount account, int index) {
		this.abstractAcc = account;
		this.index = index;
	}
	
	/**
	 * @return if this account is currently used by the player (if true, {@link #getPlayer()} cannot return a null player)
	 */
	public boolean isCurrent() {
		return abstractAcc.isCurrent();
	}
	
	/**
	 * @return the OfflinePlayer instance attached to this account (no matter if the player is online or not, or if the account is the currently used)
	 */
	public OfflinePlayer getOfflinePlayer(){
		return abstractAcc.getOfflinePlayer();
	}
	
	/**
	 * @return the Player instance who own this account. If the account is not which in use by the player ({@link #isCurrent()}), this will return null.
	 */
	public Player getPlayer(){
		return abstractAcc.getPlayer();
	}
	
	public boolean hasQuestDatas(Quest quest) {
		return questDatas.containsKey(quest.getID());
	}

	public PlayerQuestDatas getQuestDatas(Quest quest) {
		PlayerQuestDatas datas = questDatas.get(quest.getID());
		if (datas == null) {
			datas = PlayersManager.manager.createPlayerQuestDatas(this, quest);
			questDatas.put(quest.getID(), datas);
		}
		return datas;
	}

	public PlayerQuestDatas removeQuestDatas(Quest quest) {
		PlayerQuestDatas removed = questDatas.remove(quest.getID());
		if (removed != null) PlayersManager.manager.playerQuestDataRemoved(this, quest, removed);
		return removed;
	}
	
	public Collection<PlayerQuestDatas> getQuestsDatas() {
		return questDatas.values();
	}
	
	public boolean hasPoolDatas(QuestPool pool) {
		return poolDatas.containsKey(pool.getID());
	}
	
	public PlayerPoolDatas getPoolDatas(QuestPool pool) {
		PlayerPoolDatas datas = poolDatas.get(pool.getID());
		if (datas == null) {
			datas = new PlayerPoolDatas(pool.getID());
			poolDatas.put(pool.getID(), datas);
		}
		return datas;
	}
	
	public PlayerPoolDatas removePoolDatas(QuestPool pool) {
		return poolDatas.remove(pool.getID());
	}
	
	public Collection<PlayerPoolDatas> getPoolDatas() {
		return poolDatas.values();
	}

	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		PlayerAccount otherAccount = (PlayerAccount) arg0;
		if (!abstractAcc.equals(otherAccount.abstractAcc)) return false;
		return true;
	}
	
	public String debugName() {
		return abstractAcc.getIdentifier() + " (#" + index + ")";
	}

	public void serialize(ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", Utils.serializeList(questDatas.values(), PlayerQuestDatas::serialize));
	}
	
}
