package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

public class BQCraftEvent extends PlayerEvent {
	
	private final InventoryClickEvent clickEvent;
	private final ItemStack result;
	private final int maxCraftable;

	public BQCraftEvent(InventoryClickEvent clickEvent, ItemStack result, int maxCraftable) {
		super((Player) clickEvent.getView().getPlayer());
		this.clickEvent = clickEvent;
		this.result = result;
		this.maxCraftable = maxCraftable;
	}
	
	public InventoryClickEvent getClickEvent() {
		return clickEvent;
	}
	
	public ItemStack getResult() {
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
