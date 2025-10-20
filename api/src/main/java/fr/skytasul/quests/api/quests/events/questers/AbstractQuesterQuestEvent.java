package fr.skytasul.quests.api.quests.events.questers;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.events.AbstractQuesterEvent;
import fr.skytasul.quests.api.quests.Quest;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractQuesterQuestEvent extends AbstractQuesterEvent {

	protected final @NotNull Quest quest;

	protected AbstractQuesterQuestEvent(@NotNull Quester acc, @NotNull Quest quest) {
		super(acc);
		this.quest = quest;
	}

	public @NotNull Quest getQuest() {
		return quest;
	}

}
