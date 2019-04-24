package fr.skytasul.quests.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.skytasul.quests.Quest;

public abstract class QuestEvent extends Event {

	protected Quest qu;
	
	public QuestEvent(Quest quest){
		this.qu = quest;
	}

	public Quest getQuest(){
		return qu;
	}

	public HandlerList getHandlers(){
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	  return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
