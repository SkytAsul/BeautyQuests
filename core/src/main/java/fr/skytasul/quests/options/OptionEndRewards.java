package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionRewards;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionEndRewards extends QuestOptionRewards {
	
	@Override
	protected void attachedAsyncReward(AbstractReward reward) {
		getAttachedQuest().asyncEnd = true;
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.CHEST;
	}
	
	@Override
	public String getItemName() {
		return Lang.ending.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.rewardsLore.toString();
	}
	
}
