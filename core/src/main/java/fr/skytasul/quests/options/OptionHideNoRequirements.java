package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionHideNoRequirements extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.hideNoRequirements.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.hideNoRequirementsLore.toString();
	}
	
}
