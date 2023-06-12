package fr.skytasul.quests.gui.creation.stages;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;

public class StageCreationContextImplementation<T extends AbstractStage> implements StageCreationContext<T> {

	private final @NotNull StageGuiLine line;
	private final @NotNull StageType<T> type;
	private final boolean ending;
	private @Nullable StagesGUI endingBranch;
	private @Nullable StageCreation<T> creation;

	public StageCreationContextImplementation(@NotNull StageGuiLine line, @NotNull StageType<T> type, boolean ending) {
		this.line = line;
		this.type = type;
		this.ending = ending;
	}

	@Override
	public @NotNull StageGuiLine getLine() {
		return line;
	}

	@Override
	public @NotNull StageType<T> getType() {
		return type;
	}

	@Override
	public boolean isEndingStage() {
		return ending;
	}

	public @Nullable StagesGUI getEndingBranch() {
		return endingBranch;
	}

	public void setEndingBranch(StagesGUI endingBranch) {
		this.endingBranch = endingBranch;
	}

	@Override
	public @NotNull StageCreation<T> getCreation() {
		return Objects.requireNonNull(creation);
	}

	void setCreation(StageCreation<T> creation) {
		this.creation = creation;
	}

	@Override
	public void remove() {}

	@Override
	public void reopenGui() {}

	@Override
	public void removeAndReopenGui() {}

}
