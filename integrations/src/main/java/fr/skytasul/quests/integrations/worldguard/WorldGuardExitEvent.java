package fr.skytasul.quests.integrations.worldguard;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import java.util.Set;

public class WorldGuardExitEvent extends PlayerEvent {
	
	private final Set<ProtectedRegion> exited;
	
	WorldGuardExitEvent(Player who, Set<ProtectedRegion> exited) {
		super(who);
		this.exited = exited;
	}
	
	public Set<ProtectedRegion> getRegionsExited() {
		return exited;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
}
