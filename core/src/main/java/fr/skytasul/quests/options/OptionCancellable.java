package fr.skytasul.quests.options;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOptionBoolean;

public class OptionCancellable extends QuestOptionBoolean {
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return QuestsConfiguration.getMenuConfig().allowPlayerCancelQuest();
	}
	
	@Override
	public String getName() {
		return Lang.cancellable.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.cancellableLore.toString();
	}
	
}
