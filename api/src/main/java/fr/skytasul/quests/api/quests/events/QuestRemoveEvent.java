package fr.skytasul.quests.api.quests.events;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.quests.Quest;

/**
 * Called when a quest is removed<br>
 * <b>May be called in a quest editing</b>
 */
public class QuestRemoveEvent extends AbstractQuestEvent{
	
	public QuestRemoveEvent(@NotNull Quest quest) {
		super(quest);
	}

}
