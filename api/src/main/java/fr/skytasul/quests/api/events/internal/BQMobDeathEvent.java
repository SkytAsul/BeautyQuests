package fr.skytasul.quests.api.events.internal;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

public class BQMobDeathEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Object pluginEntity;
	private final Entity bukkitEntity;
	private final int amount;
	private Entity killer;

	public BQMobDeathEvent(@NotNull Object pluginEntity, @NotNull Entity killer, @Nullable Entity bukkitEntity, int amount) {
		this.pluginEntity = pluginEntity;
		this.killer = killer;
		this.bukkitEntity = bukkitEntity;
		this.amount = amount;
	}

	public @NotNull Object getPluginMob() {
		return pluginEntity;
	}

	public @NotNull Entity getKiller() {
		return killer;
	}

	public void setKiller(@NotNull Entity killer) {
		this.killer = killer;
	}

	public @Nullable Entity getBukkitEntity() {
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