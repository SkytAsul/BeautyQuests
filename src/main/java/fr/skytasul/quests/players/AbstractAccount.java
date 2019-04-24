package fr.skytasul.quests.players;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public abstract class AbstractAccount {
	
	protected AbstractAccount(){}
	
	public abstract OfflinePlayer getOfflinePlayer();
	
	public abstract Player getPlayer();
	
	public abstract boolean isCurrent();
	
	public abstract String getIdentifier();
	
	protected abstract boolean equalsAccount(AbstractAccount acc);
	
	
	public abstract int hashCode();
	
	
	public boolean equals(Object arg0) {
		if (arg0 == this) return true;
		if (arg0.getClass() != this.getClass()) return false;
		return equalsAccount((AbstractAccount) arg0);
	}
	
}
