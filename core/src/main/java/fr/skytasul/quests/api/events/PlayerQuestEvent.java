package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.events.accounts.PlayerAccountEvent;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.Quest;

public abstract class PlayerQuestEvent extends PlayerAccountEvent {

	protected final @NotNull Quest quest;
	
	protected PlayerQuestEvent(@NotNull Player who, @NotNull Quest quest) {
		super(PlayersManager.getPlayerAccount(who));
		this.quest = quest;
	}

	protected PlayerQuestEvent(@NotNull PlayerAccount acc, @NotNull Quest quest) {
		super(acc);
		this.quest = quest;
	}

	public @NotNull Quest getQuest() {
		return quest;
	}

}
