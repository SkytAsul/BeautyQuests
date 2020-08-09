package fr.skytasul.quests.api.options;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.Lang;

public abstract class QuestOptionRewards extends QuestOptionObject<AbstractReward> {

	@Override
	protected void attachObject(AbstractReward object) {
		super.attachObject(object);
		if (object.isAsync()) attachedAsyncReward(object);
	}
	
	protected abstract void attachedAsyncReward(AbstractReward reward);
	
	@Override
	protected Function<AbstractReward, Map<String, Object>> getSerializeFunction() {
		return AbstractReward::serialize;
	}

	@Override
	protected AbstractReward deserialize(Map<String, Object> map) throws ClassNotFoundException {
		return AbstractReward.deserialize(map);
	}

	@Override
	protected String getSizeString(int size) {
		return Lang.rewards.format(size);
	}

	@Override
	protected String getInventoryName() {
		return Lang.INVENTORY_REWARDS.toString();
	}

	@Override
	protected Collection<QuestObjectCreator<AbstractReward>> getCreators() {
		return QuestsAPI.rewards.values();
	}
	
}