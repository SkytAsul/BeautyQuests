package fr.skytasul.quests.options;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionName extends QuestOptionString {
	
	@Override
	public void sendIndication(Player p) {
		Lang.QUEST_NAME.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.NAME_TAG;
	}
	
	@Override
	public String getItemName() {
		return Lang.questName.toString();
	}
	
}
