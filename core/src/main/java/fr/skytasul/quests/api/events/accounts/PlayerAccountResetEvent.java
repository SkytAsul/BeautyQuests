package fr.skytasul.quests.api.events.accounts;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.players.PlayerAccount;

public class PlayerAccountResetEvent extends PlayerAccountEvent {
	
	public PlayerAccountResetEvent(Player who, PlayerAccount account) {
		super(who, account);
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
