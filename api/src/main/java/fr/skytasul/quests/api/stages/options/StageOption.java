package fr.skytasul.quests.api.stages.options;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageHandler;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public abstract class StageOption<T extends AbstractStage> extends SerializableObject implements StageHandler {

	private final @NotNull Class<T> stageClass;

	private @Nullable StageController controller;

	protected StageOption(@NotNull Class<T> stageClass) {
		super(QuestsAPI.getAPI().getStages()
				.getType(stageClass)
				.orElseThrow(() -> new IllegalArgumentException(stageClass.getName() + "has not been registered as a stage type via the API."))
				.getOptionsRegistry());
		this.stageClass = stageClass;
	}

	public @NotNull Class<T> getStageClass() {
		return stageClass;
	}

	@Override
	public void stageLoad(@NotNull StageController stage) {
		if (this.controller != null)
			throw new IllegalArgumentException("Stage already loaded");
		this.controller = stage;
	}

	@Override
	public void stageUnload(@NotNull StageController stage) {
		if (this.controller == null)
			throw new IllegalArgumentException("Stage was not loaded");
		this.controller = null;
	}

	public @NotNull Optional<StageController> getStageController() {
		return Optional.ofNullable(this.controller);
	}

	@Override
	public abstract @NotNull StageOption<T> clone();

	public abstract void startEdition(@NotNull StageCreation<T> creation);

	public abstract boolean shouldSave();

}
