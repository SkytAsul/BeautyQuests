package fr.skytasul.quests.options;

import org.bukkit.entity.Player;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.utils.Lang;

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
	
	@Override
	public String getItemDescription() {
		return Lang.customConfirmMessageLore.toString();
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return QuestsConfiguration.questConfirmGUI();
	}
	
}
