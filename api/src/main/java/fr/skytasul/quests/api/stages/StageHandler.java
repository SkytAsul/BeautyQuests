package fr.skytasul.quests.api.stages;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.players.PlayerAccount;

public interface StageHandler {
	
	default void stageStart(@NotNull PlayerAccount acc, @NotNull StageController stage) {}
	
	default void stageEnd(@NotNull PlayerAccount acc, @NotNull StageController stage) {}
	
	default void stageJoin(@NotNull Player p, @NotNull StageController stage) {}
	
	default void stageLeave(@NotNull Player p, @NotNull StageController stage) {}
	
	default void stageLoad(@NotNull StageController stage) {}
	
	default void stageUnload(@NotNull StageController stage) {}
	
	default void stageUpdated(@NotNull Player player, @NotNull StageController stage) {}

}