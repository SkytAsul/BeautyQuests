package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionBypassLimit extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.bypass.toString();
	}
	
}
