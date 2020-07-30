package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.structure.Quest;

/**
 * Called when a player finish a quest
 */
public class QuestFinishEvent extends PlayerQuestEvent{
	
	public QuestFinishEvent(Player who, Quest quest){
		super(who, quest);
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private static final HandlerList handlers = new HandlerList();

}
