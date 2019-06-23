package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.Quest;

/**
 * Called when a player finish a stage
 */
public class NextStageEvent extends PlayerQuestEvent{

	AbstractStage last, next;
	
	public NextStageEvent(Player who, Quest quest, AbstractStage last, AbstractStage next){
		super(who, quest);
		this.last = last;
		this.next = next;
	}
	
	public AbstractStage getLastStage(){
		return last;
	}
	
	public AbstractStage getNextStage(){
		return next;
	}
	
	public BranchesManager getStageManager(){
		return qu.getStageManager();
	}

}
