package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionAutoQuest extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.auto.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.autoLore.toString();
	}
	
}
