package fr.skytasul.quests.utils.compatibility.worldguard;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardEntryEvent extends PlayerEvent {
	
	private final Set<ProtectedRegion> entered;
	
	WorldGuardEntryEvent(Player who, Set<ProtectedRegion> entered) {
		super(who);
		this.entered = entered;
	}
	
	public Set<ProtectedRegion> getRegionsEntered() {
		return entered;
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
