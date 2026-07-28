package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionString;

public class OptionName extends QuestOptionString {
	
	@Override
	public String getIndication() {
		return Lang.QUEST_NAME.getValue();
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.NAME_TAG;
	}
	
	@Override
	public String getItemName() {
		return Lang.questName.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.questNameLore.toString();
	}
	
}
