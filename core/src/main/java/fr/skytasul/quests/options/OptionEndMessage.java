package fr.skytasul.quests.options;

import org.bukkit.entity.Player;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionString;

public class OptionEndMessage extends QuestOptionString {
	
	@Override
	public void sendIndication(Player p) {
		Lang.WRITE_END_MESSAGE.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.PAPER;
	}
	
	@Override
	public String getItemName() {
		return Lang.endMessage.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.endMessageLore.toString();
	}
	
}
