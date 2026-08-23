package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionString;

public class OptionEndSound extends QuestOptionString {
	
	@Override
	public String getIndication() {
		return Lang.WRITE_END_SOUND.getValue();
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.JUKEBOX;
	}
	
	@Override
	public String getItemName() {
		return Lang.endSound.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.endSoundLore.toString();
	}
	
}
