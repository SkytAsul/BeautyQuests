package fr.skytasul.quests.api.events;

import fr.skytasul.quests.structure.Quest;

/**
 * Called when a quest is removed<br>
 * <b>May be called in a quest editing</b>
 */
public class QuestRemoveEvent extends QuestEvent{
	
	public QuestRemoveEvent(Quest quest) {
		super(quest);
	}

}
