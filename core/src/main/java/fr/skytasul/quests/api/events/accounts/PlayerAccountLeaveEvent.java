package fr.skytasul.quests.api.events.accounts;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.players.PlayerAccount;

public class PlayerAccountLeaveEvent extends PlayerAccountEvent {

	public PlayerAccountLeaveEvent(@NotNull PlayerAccount account) {
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
