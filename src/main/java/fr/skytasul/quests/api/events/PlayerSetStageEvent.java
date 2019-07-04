package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.Quest;

/**
 * Called when a player finish a stage
 */
public class PlayerSetStageEvent extends PlayerQuestEvent{

	private AbstractStage stage;
	
	public PlayerSetStageEvent(Player who, Quest quest, AbstractStage stage){
		super(who, quest);
		this.stage = stage;
	}
	
	public AbstractStage getStage(){
		return stage;
	}
	
	public BranchesManager getStageManager(){
		return qu.getBranchesManager();
	}

}
