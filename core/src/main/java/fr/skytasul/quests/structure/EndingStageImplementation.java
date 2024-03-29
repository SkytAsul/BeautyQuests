package fr.skytasul.quests.structure;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.quests.branches.EndingStage;

public class EndingStageImplementation implements EndingStage {

	private final @NotNull StageControllerImplementation<?> stage;
	private final @Nullable QuestBranchImplementation branch;

	public EndingStageImplementation(@NotNull StageControllerImplementation<?> stage,
			@Nullable QuestBranchImplementation branch) {
		this.stage = Objects.requireNonNull(stage);
		this.branch = branch;
	}

	@Override
	public @NotNull StageControllerImplementation<?> getStage() {
		return stage;
	}

	@Override
	public @Nullable QuestBranchImplementation getBranch() {
		return branch;
	}

}
