package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOptionString;
import org.bukkit.entity.Player;

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
		return QuestsConfiguration.getConfig().getQuestsConfig().questConfirmGUI();
	}
	
}
