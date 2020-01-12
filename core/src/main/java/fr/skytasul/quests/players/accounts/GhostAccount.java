package fr.skytasul.quests.players.accounts;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class GhostAccount extends AbstractAccount{
	
	private String identifier;
	
	public GhostAccount(String identifier) {
		this.identifier = identifier;
	}
	
	public OfflinePlayer getOfflinePlayer(){
		throw new UnsupportedOperationException("This operation is unavailable with a ghost account");
	}
	
	public Player getPlayer(){
		//throw new UnsupportedOperationException("This operation is unavailable with a ghost account");
		return null;
	}
	
	public boolean isCurrent(){
		//throw new UnsupportedOperationException("This operation is unavailable with a ghost account");
		return false;
	}
	
	public int hashCode(){
		throw new UnsupportedOperationException("This operation is unavailable with a ghost account");
	}
	
	protected boolean equalsAccount(AbstractAccount acc){
		return ((GhostAccount) acc).identifier.equals(identifier);
	}
	
	public String getIdentifier(){
		return identifier;
	}
	
}
