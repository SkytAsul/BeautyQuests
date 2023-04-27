package fr.skytasul.quests.api.stages;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.players.PlayerAccount;

public interface StageHandler {
	
	default void stageStart(PlayerAccount acc, StageController stage) {}
	
	default void stageEnd(PlayerAccount acc, StageController stage) {}
	
	default void stageJoin(Player p, StageController stage) {}
	
	default void stageLeave(Player p, StageController stage) {}
	
	default void stageLoad(StageController stage) {}
	
	default void stageUnload(StageController stage) {}
	
}