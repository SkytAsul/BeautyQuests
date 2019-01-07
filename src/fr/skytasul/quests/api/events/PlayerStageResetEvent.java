package fr.skytasul.quests.api.events;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.players.PlayerAccount;

public class PlayerStageResetEvent extends PlayerAccountQuestEvent {

	public PlayerStageResetEvent(PlayerAccount account, Quest quest) {
		super(account, quest);
	}

}
