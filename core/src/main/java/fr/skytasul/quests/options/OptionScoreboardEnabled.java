package fr.skytasul.quests.options;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;

public class OptionScoreboardEnabled extends QuestOptionBoolean {
	
	@Override
	public String getName() {
		return Lang.scoreboard.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.scoreboardLore.toString();
	}
	
}
