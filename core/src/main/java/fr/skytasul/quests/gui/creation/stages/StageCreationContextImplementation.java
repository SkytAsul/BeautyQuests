package fr.skytasul.quests.gui.creation.stages;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

public class StageCreationContextImplementation<T extends AbstractStage> implements StageCreationContext<T> {

	private final @NotNull StageGuiLine line;
	private final @NotNull StageType<T> type;
	private final @NotNull StagesGUI gui;
	private final boolean ending;
	private @Nullable StagesGUI endingBranch;
	private @Nullable StageCreation<T> creation;

	public StageCreationContextImplementation(@NotNull StageGuiLine line, @NotNull StageType<T> type, boolean ending,
			StagesGUI gui) {
		this.line = line;
		this.type = type;
		this.ending = ending;
		this.gui = gui;
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
	public void remove() {
		gui.deleteStageLine(line);
	}

	@Override
	public void reopenGui() {
		gui.reopen();
	}

	@Override
	public void removeAndReopenGui() {
		gui.deleteStageLine(line);
		gui.reopen();
	}

}
