package fr.skytasul.quests.players.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.players.PlayerAccount;

public class PlayerAccountJoinEvent extends PlayerAccountEvent {

	private boolean firstJoin;

	public PlayerAccountJoinEvent(Player who, PlayerAccount account, boolean firstJoin) {
		super(who);
		this.account = account;
		this.firstJoin = firstJoin;
	}

	public boolean isFirstJoin() {
		return firstJoin;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
