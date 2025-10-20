package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionRewards;

public class OptionStartRewards extends QuestOptionRewards {
	
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
