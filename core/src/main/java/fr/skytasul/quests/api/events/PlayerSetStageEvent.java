package fr.skytasul.quests.api.events;

import org.bukkit.event.HandlerList;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.Quest;

/**
 * Called when a player finish a stage
 */
public class PlayerSetStageEvent extends PlayerAccountQuestEvent{

	private AbstractStage stage;
	
	public PlayerSetStageEvent(PlayerAccount account, Quest quest, AbstractStage stage){
		super(account, quest);
		this.stage = stage;
	}
	
	public AbstractStage getStage(){
		return stage;
	}
	
	public BranchesManager getStageManager(){
		return qu.getBranchesManager();
	}

	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
}
