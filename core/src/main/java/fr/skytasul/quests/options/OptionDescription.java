package fr.skytasul.quests.options;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionDescription extends QuestOptionString {
	
	@Override
	public void sendIndication(Player p) {
		Lang.QUEST_DESCRIPTION.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.OAK_SIGN;
	}
	
	@Override
	public String getItemName() {
		return Lang.customDescription.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.customDescriptionLore.toString();
	}
	
	@Override
	public boolean isMultiline() {
		return true;
	}
	
}
