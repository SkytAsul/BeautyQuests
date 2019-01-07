package fr.skytasul.quests.utils.compatibility.mobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CompatMobDeathEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final Object pluginEntity;
	private final Player killer;
	private Entity bukkitEntity;

	public CompatMobDeathEvent(Object pluginEntity, Player killer, Entity bukkitEntity) {
		this.pluginEntity = pluginEntity;
		this.killer = killer;
		this.bukkitEntity = bukkitEntity;
	}

	public Entity getBukkitEntity(){
		return bukkitEntity;
	}
	
	public Object getPluginMob() {
		return this.pluginEntity;
	}

	public Player getKiller() {
		return this.killer;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}