package fr.skytasul.quests.players;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerAccount {

	public final AbstractAccount abstractAcc;
	private UUID lastId; // cache
	
	public PlayerAccount(AbstractAccount account){
		this.abstractAcc = account;
	}
	
	public UUID getUUID(){
		if (lastId == null) lastId = abstractAcc.getOfflinePlayer().getUniqueId();
		return lastId;
	}
	
	public boolean isCurrent() {
		return abstractAcc.isCurrent();
	}
	
	public OfflinePlayer getOfflinePlayer(){
		return abstractAcc.getOfflinePlayer();
	}
	
	public Player getPlayer(){
		return abstractAcc.getPlayer();
	}
	
	
	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		return abstractAcc.equalsAccount(((PlayerAccount) arg0).abstractAcc);
	}
	
	public String getIndex(){
		return PlayersManager.getAccountIndex(this) + "";
	}
	
}
