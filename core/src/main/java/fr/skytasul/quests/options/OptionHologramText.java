package fr.skytasul.quests.options;

import org.bukkit.entity.Player;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.api.utils.XMaterial;

public class OptionHologramText extends QuestOptionString {
	
	public OptionHologramText() {
		super(OptionStarterNPC.class);
	}
	
	@Override
	public void sendIndication(Player p) {
		Lang.HOLOGRAM_TEXT.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.PAINTING;
	}
	
	@Override
	public String getItemName() {
		return Lang.hologramText.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.hologramTextLore.toString();
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return !QuestsConfigurationImplementation.getConfiguration().isTextHologramDisabled()
				&& QuestsAPI.getAPI().hasHologramsManager() && options.getOption(OptionStarterNPC.class).getValue() != null;
	}
	
}
