package fr.skytasul.quests.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.players.PlayerAccount;

public abstract class PlayerAccountQuestEvent extends Event {

	protected Quest qu;
	protected PlayerAccount acc;

	public PlayerAccountQuestEvent(PlayerAccount account, Quest quest) {
		this.acc = account;
		this.qu = quest;
	}

	public Quest getQuest() {
		return qu;
	}

	public PlayerAccount getPlayerAccount() {
		return acc;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
