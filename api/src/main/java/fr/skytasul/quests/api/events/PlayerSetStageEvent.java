package fr.skytasul.quests.api.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.QuestBranchesManager;
import fr.skytasul.quests.api.stages.StageController;

/**
 * Called when a player finish a stage
 */
public class PlayerSetStageEvent extends PlayerQuestEvent {

	private final @NotNull StageController stage;
	
	public PlayerSetStageEvent(@NotNull Quester account, @NotNull Quest quest, @NotNull StageController stage) {
		super(account, quest);
		this.stage = stage;
	}
	
	public @NotNull StageController getStage() {
		return stage;
	}
	
	public QuestBranchesManager getStageManager() {
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
