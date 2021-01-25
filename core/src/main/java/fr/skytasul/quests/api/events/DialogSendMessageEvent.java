package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.Message;
import net.citizensnpcs.api.npc.NPC;

public class DialogSendMessageEvent extends Event implements Cancellable {
	
	private boolean cancelled = false;
	
	private Dialog dialog;
	private Message msg;
	private NPC npc;
	private Player player;
	
	public DialogSendMessageEvent(Dialog dialog, Message msg, NPC npc, Player player) {
		this.dialog = dialog;
		this.msg = msg;
		this.npc = npc;
		this.player = player;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
	
	public Dialog getDialog() {
		return dialog;
	}
	
	public Message getMessage() {
		return msg;
	}
	
	public NPC getNPC() {
		return npc;
	}
	
	public Player getPlayer(){
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
