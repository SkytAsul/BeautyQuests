package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.skytasul.quests.QuestsConfiguration.ClickType;
import fr.skytasul.quests.api.npcs.BQNPC;

public class BQNPCClickEvent extends PlayerEvent implements Cancellable {
	
	private final BQNPC npc;
	private final ClickType click;
	
	private boolean cancelled = false;
	
	public BQNPCClickEvent(BQNPC npc, Player p, ClickType click) {
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
	
	public BQNPC getNPC() {
		return npc;
	}
	
	public ClickType getClick() {
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
