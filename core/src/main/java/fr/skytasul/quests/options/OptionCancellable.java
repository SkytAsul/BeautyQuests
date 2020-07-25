package fr.skytasul.quests.options;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.utils.Lang;

public class OptionCancellable extends QuestOptionBoolean {
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return QuestsConfiguration.allowPlayerCancelQuest();
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
