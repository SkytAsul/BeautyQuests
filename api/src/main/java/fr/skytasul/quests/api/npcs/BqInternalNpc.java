package fr.skytasul.quests.api.npcs;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface BqInternalNpc {

	public int getInternalId();

	public abstract @NotNull String getName();

	public abstract boolean isSpawned();

	public abstract @NotNull Entity getEntity();

	public abstract @NotNull Location getLocation();

	/**
	 * Sets the "paused" state of the NPC navigation
	 * 
	 * @param paused should the navigation be paused
	 * @return <code>true</code> if the navigation was paused before this call, <code>false</code>
	 *         otherwise
	 */
	public abstract boolean setNavigationPaused(boolean paused);

}
