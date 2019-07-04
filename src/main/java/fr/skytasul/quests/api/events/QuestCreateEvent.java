package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import fr.skytasul.quests.Quest;

/**
 * Called when a quest is created
 */
public class QuestCreateEvent extends PlayerQuestEvent implements Cancellable{

	private boolean cancel, edit = false;
	
	public QuestCreateEvent(Player who, Quest quest, boolean edit){
		super(who, quest);
		this.edit = edit;
	}

	public boolean isCancelled(){
		return cancel;
	}

	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
	}
	
	public boolean isEdited(){
		return edit;
	}

}
