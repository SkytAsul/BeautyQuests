package fr.skytasul.quests.players.events;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import fr.skytasul.quests.players.PlayerAccount;

public abstract class PlayerAccountEvent extends PlayerEvent {
	
	protected PlayerAccount account;

	public PlayerAccountEvent(Player who) {
		super(who);
	}

	public PlayerAccount getPlayerAccount() {
		return account;
	}
	
}