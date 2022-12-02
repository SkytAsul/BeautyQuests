package fr.skytasul.quests.utils.compatibility.mobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CompatMobDeathEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	private final Object pluginEntity;
	private final Player killer;
	private final Entity bukkitEntity;
	private final int amount;

	public CompatMobDeathEvent(Object pluginEntity, Player killer, Entity bukkitEntity, int amount) {
		this.pluginEntity = pluginEntity;
		this.killer = killer;
		this.bukkitEntity = bukkitEntity;
		this.amount = amount;
	}
	
	public Object getPluginMob() {
		return pluginEntity;
	}

	public Player getKiller() {
		return killer;
	}

	public Entity getBukkitEntity() {
		return bukkitEntity;
	}

	public int getAmount() {
		return amount;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}