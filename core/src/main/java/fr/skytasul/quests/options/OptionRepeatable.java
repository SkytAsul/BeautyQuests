package fr.skytasul.quests.options;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.structure.QuestImplementation;
import java.util.ArrayList;
import java.util.List;

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
		if (quest.testTimer(context.getQuester(), false)) {
			lore.add(Lang.canRedo.toString());
		}else {
			lore.add(Lang.timeWait.quickFormat("time_left", quest.getTimeLeft(context.getQuester())));
		}
		lore.add(null);
		lore.add(Lang.timesFinished.quickFormat("times_finished",
				context.getQuestDatas().map(x -> x.getTimesFinished()).orElse(0)));
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
