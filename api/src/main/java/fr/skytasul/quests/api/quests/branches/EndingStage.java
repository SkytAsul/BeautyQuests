package fr.skytasul.quests.api.quests.branches;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.stages.StageController;

public interface EndingStage {

	public @NotNull StageController getStage();

	public @Nullable QuestBranch getBranch();

}
