package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import fr.skytasul.quests.Quest;

/**
 * Called when a player starts a quest
 */
public class QuestLaunchEvent extends PlayerQuestEvent implements Cancellable{

	private boolean cancel = false;
	
	public QuestLaunchEvent(Player who, Quest quest){
		super(who, quest);
	}

	public boolean isCancelled(){
		return cancel;
	}

	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
	}

}
