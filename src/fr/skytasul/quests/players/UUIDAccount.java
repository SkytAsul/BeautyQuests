package fr.skytasul.quests.players;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class UUIDAccount extends AbstractAccount{

	private UUID id;
	
	UUIDAccount(UUID id){
		this.id = id;
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(id);
	}

	public String getIdentifier() {
		return id.toString();
	}

	public boolean isCurrent() {
		return getOfflinePlayer().isOnline();
	}

	protected boolean equalsAccount(AbstractAccount acc) {
		return ((UUIDAccount) acc).id.equals(this.id);
	}

	
	public int hashCode() {
		return id.hashCode();
	}

}
