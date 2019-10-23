package fr.skytasul.quests.players;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerAccountJoinEvent extends PlayerEvent {

	private PlayerAccount account;
	private boolean firstJoin;

	public PlayerAccountJoinEvent(Player who, PlayerAccount account, boolean firstJoin) {
		super(who);
		this.account = account;
		this.firstJoin = firstJoin;
	}

	public PlayerAccount getPlayerAccount(){
		return account;
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
