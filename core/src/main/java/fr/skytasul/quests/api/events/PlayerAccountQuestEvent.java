package fr.skytasul.quests.api.events;

import org.bukkit.event.Event;

import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.Quest;

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

}
