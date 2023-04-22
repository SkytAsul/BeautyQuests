package fr.skytasul.quests.api.options;

import java.util.Map;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.utils.Lang;

public abstract class QuestOptionRewards extends QuestOptionObject<AbstractReward, RewardCreator> {

	@Override
	protected void attachObject(AbstractReward object) {
		super.attachObject(object);
		if (object.isAsync()) attachedAsyncReward(object);
	}
	
	protected abstract void attachedAsyncReward(AbstractReward reward);

	@Override
	protected AbstractReward deserialize(Map<String, Object> map) {
		return AbstractReward.deserialize(map);
	}

	@Override
	protected String getSizeString(int size) {
		return Lang.rewards.format(size);
	}
	
	@Override
	protected QuestObjectsRegistry<AbstractReward, RewardCreator> getObjectsRegistry() {
		return QuestsAPI.getRewards();
	}
	
}