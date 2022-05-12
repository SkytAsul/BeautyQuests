package fr.skytasul.quests.api.stages.options;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.serializable.SerializableObject;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.StageHandler;

public abstract class StageOption<T extends AbstractStage> extends SerializableObject implements StageHandler {
	
	protected StageOption(Class<T> stageClass) {
		super(QuestsAPI.getStageType(stageClass).getOptionsRegistry());
	}
	
	protected StageOption(SerializableCreator<StageOption<T>> creator) {
		super(creator);
	}
	
	@Override
	public abstract StageOption<T> clone();
	
	public abstract boolean shouldSave();
	
	public abstract void startEdition(StageCreation<T> creation);
	
}
