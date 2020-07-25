package fr.skytasul.quests.options;

import java.util.ArrayList;

import fr.skytasul.quests.api.options.QuestOptionRewards;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionStartRewards extends QuestOptionRewards {
	
	@Override
	protected void addedAsyncReward() {
		getAttachedQuest().asyncStart = new ArrayList<>();
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
