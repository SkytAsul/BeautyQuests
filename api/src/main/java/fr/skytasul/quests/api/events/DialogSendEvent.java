package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;

public class DialogSendEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled = false;

	private final @NotNull DialogRunner dialog;

	public DialogSendEvent(@NotNull Player who, @NotNull DialogRunner dialog) {
		super(who);
		this.dialog = dialog;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
	public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

	public @NotNull DialogRunner getDialog() {
		return dialog;
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
