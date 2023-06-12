package fr.skytasul.quests.api.stages;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.quests.branches.QuestBranch;

public interface StageController {

	public @NotNull QuestBranch getBranch();

	public @NotNull AbstractStage getStage();

	public @NotNull StageType<?> getStageType();

	public void finishStage(@NotNull Player player);

	public boolean hasStarted(@NotNull PlayerAccount acc);

	public void updateObjective(@NotNull Player player, @NotNull String dataKey, @Nullable Object dataValue);

	public @NotNull String getDescriptionLine(@NotNull PlayerAccount acc, @NotNull DescriptionSource source);

	public <T> @Nullable T getData(@NotNull PlayerAccount acc, @NotNull String dataKey);

	public @NotNull String getFlowId();

}
