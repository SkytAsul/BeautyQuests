package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionRepeatable extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.multiple.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.multipleLore.toString();
	}
	
}
