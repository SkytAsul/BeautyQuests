package fr.skytasul.quests.options;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionConfirmMessage extends QuestOptionString {
	
	@Override
	public void sendIndication(Player p) {
		Lang.CONFIRM_MESSAGE.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.FEATHER;
	}
	
	@Override
	public String getItemName() {
		return Lang.customConfirmMessage.toString();
	}
	
}
