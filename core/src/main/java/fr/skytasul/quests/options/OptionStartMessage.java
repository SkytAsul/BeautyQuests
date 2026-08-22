package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionString;

public class OptionStartMessage extends QuestOptionString {
	
	@Override
	public String getIndication() {
		return Lang.WRITE_START_MESSAGE.getValue();
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.PAPER;
	}
	
	@Override
	public String getItemName() {
		return Lang.startMessage.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.startMessageLore.toString();
	}
	
}
