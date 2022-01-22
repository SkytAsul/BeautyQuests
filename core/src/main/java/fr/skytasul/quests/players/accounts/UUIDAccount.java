package fr.skytasul.quests.players.accounts;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class UUIDAccount extends AbstractAccount{

	private UUID id;
	
	public UUIDAccount(UUID id) {
		this.id = id;
	}
	
	@Override
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(id);
	}

	@Override
	public Player getPlayer(){
		return Bukkit.getPlayer(id);
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	@Override
	public boolean isCurrent() {
		Player p = getPlayer();
		return p != null && p.isOnline();
	}

	@Override
	protected boolean equalsAccount(AbstractAccount acc) {
		return ((UUIDAccount) acc).id.equals(this.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
