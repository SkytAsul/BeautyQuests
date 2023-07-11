package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.Message;

public class DialogSendMessageEvent extends Event implements Cancellable {
	
	private boolean cancelled = false;
	
	private final @NotNull Dialog dialog;
	private final @NotNull Message msg;
	private final @NotNull BqNpc npc;
	private final @NotNull Player player;
	
	public DialogSendMessageEvent(@NotNull Dialog dialog, @NotNull Message msg, @NotNull BqNpc npc, @NotNull Player player) {
		this.dialog = dialog;
		this.msg = msg;
		this.npc = npc;
		this.player = player;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public @NotNull Dialog getDialog() {
		return dialog;
	}
	
	public @NotNull Message getMessage() {
		return msg;
	}
	
	public @NotNull BqNpc getNPC() {
		return npc;
	}
	
	public @NotNull Player getPlayer() {
		return player;
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
