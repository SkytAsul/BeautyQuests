package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import fr.skytasul.quests.structure.Quest;

/**
 * Called before a player starts a quest
 */
public class QuestPreLaunchEvent extends PlayerQuestEvent implements Cancellable{

	private boolean cancel = false;
	
	public QuestPreLaunchEvent(Player who, Quest quest){
		super(who, quest);
	}

	public boolean isCancelled(){
		return cancel;
	}

	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
	}

}
