package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

public interface StageController {

	public @NotNull QuestBranch getBranch();

	public @NotNull AbstractStage getStage();

	public @NotNull StageType<?> getStageType();

	/**
	 * Constructs a collection of questers that should get advancement applied when the player does
	 * something for the stage.
	 *
	 * @param player Player that did an action regarding the stage
	 * @return a collection of questers that should be updated
	 */
	public @NotNull Collection<Quester> getApplicableQuesters(@NotNull Player player);

	public void finishStage(@NotNull Quester quester);

	public boolean hasStarted(@NotNull Quester acc);

	public void updateObjective(@NotNull Quester quester, @NotNull String dataKey, @Nullable Object dataValue);

	public @Nullable String getDescriptionLine(@NotNull Quester acc, @NotNull DescriptionSource source);

	public <T> @Nullable T getData(@NotNull Quester acc, @NotNull String dataKey, @Nullable Class<T> dataType);

	public @NotNull String getFlowId();

}
