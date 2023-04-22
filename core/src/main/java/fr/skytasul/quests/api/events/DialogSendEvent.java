package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.types.Dialog;

public class DialogSendEvent extends Event implements Cancellable {

    private boolean cancelled = false;

	private final @NotNull Dialog dialog;
	private final @NotNull BQNPC npc;
	private final @NotNull Player player;
	private final @NotNull Runnable runnable;

	public DialogSendEvent(@NotNull Dialog dialog, @NotNull BQNPC npc, @NotNull Player player, @NotNull Runnable runnable) {
        this.dialog = dialog;
        this.npc = npc;
        this.player = player;
        this.runnable = runnable;
		// TODO change this "runnable" thing
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
	public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Dialog getDialog() {
        return dialog;
    }

	public BQNPC getNPC() {
        return npc;
    }

    public Player getPlayer(){
        return player;
    }

    public Runnable getRunnable(){
        return runnable;
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
