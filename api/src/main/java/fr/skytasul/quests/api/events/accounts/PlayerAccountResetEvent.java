package fr.skytasul.quests.api.events.accounts;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
