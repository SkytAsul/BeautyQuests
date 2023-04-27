package fr.skytasul.quests.api.events.internal;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BQCraftEvent extends PlayerEvent {
	
	private final @NotNull InventoryClickEvent clickEvent;
	private final @NotNull ItemStack result;
	private final int maxCraftable;

	public BQCraftEvent(@NotNull InventoryClickEvent clickEvent, @NotNull ItemStack result, int maxCraftable) {
		super((Player) clickEvent.getView().getPlayer());
		this.clickEvent = clickEvent;
		this.result = result;
		this.maxCraftable = maxCraftable;
	}
	
	public @NotNull InventoryClickEvent getClickEvent() {
		return clickEvent;
	}
	
	public @NotNull ItemStack getResult() {
		return result;
	}
	
	public int getMaxCraftable() {
		return maxCraftable;
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
