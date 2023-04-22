package fr.skytasul.quests.options;

import java.util.ArrayList;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.options.QuestOptionRewards;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.utils.Lang;

public class OptionStartRewards extends QuestOptionRewards {
	
	@Override
	protected void attachedAsyncReward(AbstractReward reward) {
		if (getAttachedQuest().asyncStart == null) getAttachedQuest().asyncStart = new ArrayList<>();
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.CARROT_ON_A_STICK;
	}
	
	@Override
	public String getItemName() {
		return Lang.startRewards.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.startRewardsLore.toString();
	}
	
}
