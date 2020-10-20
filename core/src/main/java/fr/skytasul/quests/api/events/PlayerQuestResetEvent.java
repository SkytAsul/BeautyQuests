package fr.skytasul.quests.api.events;

import org.bukkit.event.HandlerList;

import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;

/**
 * Called when the stage of a player is cancelled
 */
public class PlayerQuestResetEvent extends PlayerAccountQuestEvent {

	public PlayerQuestResetEvent(PlayerAccount account, Quest quest) {
		super(account, quest);
	}

	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
}
