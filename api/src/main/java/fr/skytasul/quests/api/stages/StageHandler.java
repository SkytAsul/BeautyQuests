package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.players.Quester;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface StageHandler {

	default void stageStart(@NotNull Quester quester, @NotNull StageController stage) {}

	default void stageEnd(@NotNull Quester quester, @NotNull StageController stage) {}

	default void stageJoin(@NotNull Player p, @NotNull Quester quester, @NotNull StageController stage) {}

	default void stageLeave(@NotNull Player p, @NotNull Quester quester, @NotNull StageController stage) {}

	default void stageLoad(@NotNull StageController stage) {}

	default void stageUnload(@NotNull StageController stage) {}

	default void stageUpdated(@NotNull Quester quester, @NotNull StageController stage) {}

}