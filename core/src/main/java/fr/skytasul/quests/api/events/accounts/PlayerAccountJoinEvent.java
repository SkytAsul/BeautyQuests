package fr.skytasul.quests.api.events.accounts;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.players.PlayerAccount;

public class PlayerAccountJoinEvent extends PlayerAccountEvent {

	private boolean firstJoin;

	public PlayerAccountJoinEvent(@NotNull PlayerAccount account, boolean firstJoin) {
		super(account);
		this.firstJoin = firstJoin;
	}

	public boolean isFirstJoin() {
		return firstJoin;
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
