package fr.skytasul.quests.players;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerAccount {

	public final AbstractAccount abstractAcc;
	
	public PlayerAccount(AbstractAccount account){
		this.abstractAcc = account;
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
	
	
	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		return abstractAcc.equalsAccount(((PlayerAccount) arg0).abstractAcc);
	}
	
	/**
	 * @return String identifier stored in the data file
	 */
	public String getIndex(){
		return PlayersManager.getAccountIndex(this) + "";
	}
	
}
