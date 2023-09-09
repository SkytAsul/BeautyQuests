package fr.skytasul.quests.npcs;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.NpcClickType;

public class BQNPCClickEvent extends PlayerEvent implements Cancellable {
	
	private final @NotNull BqNpc npc;
	private final @NotNull NpcClickType click;
	
	private boolean cancelled = false;
	
	public BQNPCClickEvent(@NotNull BqNpc npc, @NotNull Player p, @NotNull NpcClickType click) {
		super(p);
		this.npc = npc;
		this.click = click;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public @NotNull BqNpc getNPC() {
		return npc;
	}
	
	public @NotNull NpcClickType getClick() {
		return click;
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
