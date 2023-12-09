package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.npcs.dialogs.Message;

public class DialogSendMessageEvent extends PlayerEvent implements Cancellable {

	private boolean cancelled = false;

	private final @NotNull DialogRunner dialog;
	private final @NotNull Message message;

	public DialogSendMessageEvent(@NotNull Player who, @NotNull DialogRunner dialog, @NotNull Message message) {
		super(who);
		this.dialog = dialog;
		this.message = message;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public DialogRunner getDialog() {
		return dialog;
	}

	public Message getMessage() {
		return message;
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
