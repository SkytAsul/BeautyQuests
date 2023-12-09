package fr.skytasul.quests.options;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;

public class OptionBypassLimit extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.bypass.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.bypassLore.toString();
	}
	
}
