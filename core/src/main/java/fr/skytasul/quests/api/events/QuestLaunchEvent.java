package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;

import fr.skytasul.quests.structure.Quest;

/**
 * Called when a player starts a quest
 */
public class QuestLaunchEvent extends PlayerQuestEvent {
	
	public QuestLaunchEvent(Player who, Quest quest){
		super(who, quest);
	}

}
