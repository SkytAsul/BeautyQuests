package fr.skytasul.quests.api.events.accounts;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.players.PlayerAccount;

public class PlayerAccountJoinEvent extends PlayerAccountEvent {

	private boolean firstJoin;

	public PlayerAccountJoinEvent(Player who, PlayerAccount account, boolean firstJoin) {
		super(who, account);
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
