package fr.skytasul.quests.api.stages;

import java.util.Map;
import java.util.Map.Entry;
import fr.skytasul.quests.structure.QuestBranch;

@Deprecated
public abstract class AbstractCountableStage<T> extends fr.skytasul.quests.api.stages.types.AbstractCountableStage<T> {
	
	@Deprecated
	protected AbstractCountableStage(QuestBranch branch, Map<Integer, Entry<T, Integer>> objects) {
		super(branch, objects);
	}
	
}
