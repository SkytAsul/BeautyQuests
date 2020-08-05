package fr.skytasul.quests.options;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionStartable extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.startableFromGUI.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.startableFromGUILore.toString();
	}
	
}
