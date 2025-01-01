package fr.skytasul.quests.rewards;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.api.rewards.RewardGiveContext;

public class QuestStopReward extends AbstractReward {

	public QuestStopReward() {}

	public QuestStopReward(String customDescription) {
		super(customDescription);
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {}

	@Override
	public void give(RewardGiveContext context) {
		if (getAttachedQuest() == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("No attached quest for " + debugName());
		} else {
			getAttachedQuest().cancelPlayer(context.getQuester());
			context.interruptBranch();
		}
	}

	@Override
	public AbstractReward clone() {
		return new QuestStopReward(getCustomDescription());
	}

}
