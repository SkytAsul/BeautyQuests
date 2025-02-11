package fr.skytasul.quests.api.quests.branches;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.stages.StageController;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.Collection;

public interface QuestBranchesManager {

	@NotNull
	Quest getQuest();

	public int getId(@NotNull QuestBranch branch);

	public @UnmodifiableView @NotNull Collection<@NotNull QuestBranch> getBranches();

	public @Nullable QuestBranch getBranch(int id);

	public @Nullable QuestBranch getPlayerBranch(@NotNull Quester acc);

	public boolean hasBranchStarted(@NotNull Quester acc, @NotNull QuestBranch branch);

	public @Nullable StageController getStageFromFlow(@NotNull String flowId) throws IllegalArgumentException;

}
