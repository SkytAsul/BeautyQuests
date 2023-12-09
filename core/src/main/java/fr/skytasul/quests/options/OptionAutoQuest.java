package fr.skytasul.quests.options;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;

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
