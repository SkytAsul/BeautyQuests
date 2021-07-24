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
		return removeQuestDatas(quest.getID());
	}
	
	public PlayerQuestDatas removeQuestDatas(int id) {
		PlayerQuestDatas removed = questDatas.remove(id);
		if (removed != null) PlayersManager.manager.playerQuestDataRemoved(this, id, removed);
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
			datas = PlayersManager.manager.createPlayerPoolDatas(this, pool);
			poolDatas.put(pool.getID(), datas);
		}
		return datas;
	}
	
	public PlayerPoolDatas removePoolDatas(QuestPool pool) {
		return removePoolDatas(pool.getID());
	}
	
	public PlayerPoolDatas removePoolDatas(int id) {
		PlayerPoolDatas removed = poolDatas.remove(id);
		if (removed != null) PlayersManager.manager.playerPoolDataRemoved(this, id, removed);
		return removed;
	}
	
	public Collection<PlayerPoolDatas> getPoolDatas() {
		return poolDatas.values();
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		PlayerAccount otherAccount = (PlayerAccount) arg0;
		if (!abstractAcc.equals(otherAccount.abstractAcc)) return false;
		return true;
	}
	
	public String getName() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName();
	}
	
	public String debugName() {
		return abstractAcc.getIdentifier() + " (#" + index + ")";
	}

	public void serialize(ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", Utils.serializeList(questDatas.values(), PlayerQuestDatas::serialize));
		config.set("pools", Utils.serializeList(poolDatas.values(), PlayerPoolDatas::serialize));
	}
	
}
