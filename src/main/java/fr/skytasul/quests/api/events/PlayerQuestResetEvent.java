package fr.skytasul.quests.api.events;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.players.PlayerAccount;

/**
 * Called when the stage of a player is cancelled
 */
public class PlayerQuestResetEvent extends PlayerAccountQuestEvent {

	public PlayerQuestResetEvent(PlayerAccount account, Quest quest) {
		super(account, quest);
	}

}
