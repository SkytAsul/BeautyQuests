package fr.skytasul.quests.api.events.accounts;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import fr.skytasul.quests.players.PlayerAccount;

public abstract class PlayerAccountEvent extends Event {
	
	private Player who;
	private PlayerAccount account;

	protected PlayerAccountEvent(Player who, PlayerAccount account) {
		this.who = who;
		this.account = account;
	}
	
	public boolean isAccountCurrent() {
		return who != null;
	}
	
	public Player getPlayer() {
		return who;
	}

	public PlayerAccount getPlayerAccount() {
		return account;
	}
	
}