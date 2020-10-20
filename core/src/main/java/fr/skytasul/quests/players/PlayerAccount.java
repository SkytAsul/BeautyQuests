package fr.skytasul.quests.players;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.players.accounts.AbstractAccount;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Utils;

public class PlayerAccount {

	public final AbstractAccount abstractAcc;
	Map<Integer, PlayerQuestDatas> datas = new HashMap<>();
	int index;
	
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
		return datas.containsKey(quest.getID());
	}

	public PlayerQuestDatas getQuestDatas(Quest quest) {
		PlayerQuestDatas questDatas = datas.get(quest.getID());
		if (questDatas == null) {
			questDatas = PlayersManager.manager.createPlayerQuestDatas(this, quest);
			datas.put(quest.getID(), questDatas);
		}
		return questDatas;
	}

	public PlayerQuestDatas removeQuestDatas(Quest quest) {
		PlayerQuestDatas removed = datas.remove(quest.getID());
		if (removed != null) PlayersManager.manager.playerQuestDataRemoved(this, quest, removed);
		return removed;
	}
	
	public Collection<PlayerQuestDatas> getQuestsDatas() {
		return datas.values();
	}

	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		PlayerAccount otherAccount = (PlayerAccount) arg0;
		if (!abstractAcc.equals(otherAccount.abstractAcc)) return false;
		if (!datas.equals(otherAccount.datas)) return false;
		return true;
	}
	
	public String debugName() {
		return abstractAcc.getIdentifier() + " (#" + index + ")";
	}

	public void serialize(ConfigurationSection config) {
		config.set("identifier", abstractAcc.getIdentifier());
		config.set("quests", Utils.serializeList(datas.values(), PlayerQuestDatas::serialize));
	}
	
}
