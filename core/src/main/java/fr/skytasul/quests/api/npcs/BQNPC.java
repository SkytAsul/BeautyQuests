package fr.skytasul.quests.api.npcs;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public interface BQNPC {
	
	public abstract int getId();
	
	public abstract String getName();
	
	public abstract boolean isSpawned();
	
	public abstract Entity getEntity();
	
	public abstract Location getLocation();
	
	public abstract void setSkin(String skin);
	
	/**
	 * Sets the "paused" state of the NPC navigation
	 * 
	 * @param paused should the navigation be paused
	 * @return <code>true</code> if the navigation was
	 * paused before this call, <code>false</code> otherwise
	 */
	public abstract boolean setNavigationPaused(boolean paused);
	
}