package fr.skytasul.quests.api.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.Quest;

/**
 * Called when a player finish a stage
 */
public class PlayerSetStageEvent extends PlayerQuestEvent {

	private final @NotNull AbstractStage stage;
	
	public PlayerSetStageEvent(@NotNull PlayerAccount account, @NotNull Quest quest, @NotNull AbstractStage stage) {
		super(account, quest);
		this.stage = stage;
	}
	
	public @NotNull AbstractStage getStage() {
		return stage;
	}
	
	public BranchesManager getStageManager(){
		return quest.getBranchesManager();
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private static final HandlerList handlers = new HandlerList();
	
}
