package fr.skytasul.quests.api.events.accounts;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.players.Quester;

public class PlayerAccountResetEvent extends PlayerAccountEvent {
	
	public PlayerAccountResetEvent(@NotNull Quester account) {
		super(account);
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
