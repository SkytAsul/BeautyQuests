package fr.skytasul.quests.players.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.players.PlayerAccount;

public class PlayerAccountLeaveEvent extends PlayerAccountEvent {

	public PlayerAccountLeaveEvent(Player who, PlayerAccount account) {
		super(who);
		this.account = account;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
