package fr.skytasul.quests.api.stages;

import org.bukkit.entity.Player;

import fr.skytasul.quests.players.PlayerAccount;

public interface StageHandler {
	
	default void stageStart(PlayerAccount acc, AbstractStage stage) {}
	
	default void stageEnd(PlayerAccount acc, AbstractStage stage) {}
	
	default void stageJoin(PlayerAccount acc, Player p, AbstractStage stage) {}
	
	default void stageLeave(PlayerAccount acc, Player p, AbstractStage stage) {}
	
	default void stageLoad(AbstractStage stage) {}
	
	default void stageUnload(AbstractStage stage) {}
	
}