package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionHide extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.hide.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.hideLore.toString();
	}
	
}
