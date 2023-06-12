package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.structure.QuestImplementation;

public class OptionRepeatable extends QuestOptionBoolean implements QuestDescriptionProvider {
	
	@Override
	public String getName() {
		return Lang.multiple.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.multipleLore.toString();
	}
	
	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		if (context.getCategory() != PlayerListCategory.FINISHED) return null;
		
		List<String> lore = new ArrayList<>(4);
		QuestImplementation quest = (QuestImplementation) context.getQuest();
		if (quest.testTimer(context.getPlayerAccount(), false)) {
			lore.add(Lang.canRedo.toString());
		}else {
			lore.add(Lang.timeWait.format(quest.getTimeLeft(context.getPlayerAccount())));
		}
		lore.add(null);
		lore.add(Lang.timesFinished.format(context.getQuestDatas().getTimesFinished()));
		return lore;
	}
	
	@Override
	public String getDescriptionId() {
		return "repeatable";
	}

	@Override
	public double getDescriptionPriority() {
		return 100;
	}
	
}
