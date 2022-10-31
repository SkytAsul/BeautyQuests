package fr.skytasul.quests.players;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.data.SavableData;
import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Utils;

public class PlayerAccount {

	public static final List<String> FORBIDDEN_DATA_ID = Arrays.asList("identifier", "quests", "pools");
	
	public final AbstractAccount abstractAcc;
	protected final Map<Integer, PlayerQuestDatas> questDatas = new HashMap<>();
	protected final Map<Integer, PlayerPoolDatas> poolDatas = new HashMap<>();
	protected final Map<SavableData<?>, Object> additionalDatas = new HashMap<>();
	protected final int index;
	
	protected PlayerAccount(AbstractAccount account, int index) {
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
	
	public PlayerQuestDatas getQuestDatasIfPresent(Quest quest) {
		return questDatas.get(quest.getID());
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
	
	protected PlayerQuestDatas removeQuestDatasSilently(int id) {
		return questDatas.remove(id);
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
	
	public <T> T getData(SavableData<T> data) {
		if (!PlayersManager.manager.getAccountDatas().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		return (T) additionalDatas.getOrDefault(data, data.getDefaultValue());
	}

	public <T> void setData(SavableData<T> data, T value) {
		if (!PlayersManager.manager.getAccountDatas().contains(data))
			throw new IllegalArgumentException("The " + data.getId() + " account data has not been registered.");
		additionalDatas.put(data, value);
	}
	
	public void resetDatas() {
		additionalDatas.clear();
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		PlayerAccount otherAccount = (PlayerAccount) arg0;
		if (!abstractAcc.equals(otherAccount.abstractAcc)) return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		
		hash = hash * 31 + index;
		hash = hash * 31 + abstractAcc.hashCode();
		
		return hash;
	}
	
	public String getName() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName();
	}
	
	public String getNameAndID() {
		Player p = getPlayer();
		return p == null ? debugName() : p.getName() + " (# " + index + ")";
	}
	
	public String debugName() {
		return abstractAcc.getIdentifier() + " (#" + index + ")";
	}

	public void serialize(ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", questDatas.isEmpty() ? null : Utils.serializeList(questDatas.values(), PlayerQuestDatas::serialize));
		config.set("pools", poolDatas.isEmpty() ? null : Utils.serializeList(poolDatas.values(), PlayerPoolDatas::serialize));
		additionalDatas.entrySet().forEach(entry -> {
			config.set(entry.getKey().getId(), entry.getValue());
		});
	}
	
}
