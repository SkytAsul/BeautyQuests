package fr.skytasul.quests.api.options;

import java.util.Collection;
import java.util.Map;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.rewards.RewardList;

public abstract class QuestOptionRewards extends QuestOptionObject<AbstractReward, RewardCreator, RewardList> {

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
		return QuestsAPI.getAPI().getRewards();
	}
	
	@Override
	protected RewardList instanciate(Collection<AbstractReward> objects) {
		return new RewardList(objects);
	}

}