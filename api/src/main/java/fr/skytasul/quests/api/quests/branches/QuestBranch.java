package fr.skytasul.quests.api.quests.branches;

import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.List;

public interface QuestBranch {

	int getId();

	@NotNull
	QuestBranchesManager getManager();

	@NotNull
	default Quest getQuest() {
		return getManager().getQuest();
	}

	public @NotNull @UnmodifiableView List<@NotNull StageController> getRegularStages();

	public @NotNull StageController getRegularStage(int id);

	public @NotNull @UnmodifiableView List<EndingStage> getEndingStages();

	public @NotNull StageController getEndingStage(int id);

	public @NotNull String getDescriptionLine(@NotNull Quester acc, @NotNull DescriptionSource source);

	public boolean hasStageLaunched(@Nullable Quester acc, @NotNull StageController stage);

	/**
	 * Must be called when a quester completes a stage. This causes the stage to end and the next one to
	 * begin, or the quest to finish if it was the last stage.
	 *
	 * @param quester quester which has completed the stage
	 * @param stage that the player just completed. If the quester did not have this stage launched, the
	 *        call will fail.
	 */
	void finishPlayerStage(@NotNull Quester quester, @NotNull StageController stage);

	/**
	 * Changes which stage the player is currently doing in the branch. If the player did not have
	 * already started a stage in this branch, an exception will be raised.
	 *
	 * @param quester player for which the running stage will be changed
	 * @param stage new stage to start
	 */
	void setPlayerStage(@NotNull Quester quester, @NotNull StageController stage);

	/**
	 * Similar to {@link #setPlayerStage(Quester, StageController)} but with ending stages instead of a
	 * regular stage.
	 *
	 * @param quester player for which the ending stages will start
	 */
	void setPlayerEndingStages(@NotNull Quester quester);

}
