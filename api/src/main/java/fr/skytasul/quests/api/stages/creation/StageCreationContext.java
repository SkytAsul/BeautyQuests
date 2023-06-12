package fr.skytasul.quests.api.stages.creation;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;

public interface StageCreationContext<T extends AbstractStage> {

	@NotNull
	StageGuiLine getLine();

	@NotNull
	StageType<T> getType();

	boolean isEndingStage();

	@NotNull
	StageCreation<T> getCreation();

	void remove();

	void reopenGui();

	void removeAndReopenGui();

}
