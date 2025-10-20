package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOptionItem;

public class OptionHologramLaunch extends QuestOptionItem {
	
	public OptionHologramLaunch() {
		super(OptionStarterNPC.class);
	}
	
	@Override
	public XMaterial getDefaultMaterial() {
		return XMaterial.RED_STAINED_GLASS_PANE;
	}
	
	@Override
	public String getItemName() {
		return Lang.hologramLaunch.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.hologramLaunchLore.toString();
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return QuestsAPI.getAPI().hasHologramsManager() && options.getOption(OptionStarterNPC.class).getValue() != null;
	}
	
}
