package fr.skytasul.quests.api.stages.options;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.StageHandler;

public abstract class StageOption<T extends AbstractStage> extends SerializableObject implements StageHandler {
	
	private final Class<T> stageClass;
	
	protected StageOption(Class<T> stageClass) {
		super(QuestsAPI.getStages()
				.getType(stageClass)
				.orElseThrow(() -> new IllegalArgumentException(stageClass.getName() + "has not been registered as a stage type via the API."))
				.getOptionsRegistry());
		this.stageClass = stageClass;
	}
	
	public Class<T> getStageClass() {
		return stageClass;
	}
	
	@Override
	public abstract StageOption<T> clone();
	
	public abstract boolean shouldSave();
	
	public abstract void startEdition(StageCreation<T> creation);
	
}
