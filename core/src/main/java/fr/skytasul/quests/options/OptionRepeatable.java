package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.gui.quests.PlayerListGUI.Category;
import fr.skytasul.quests.utils.Lang;

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
		if (context.getCategory() != Category.FINISHED) return null;
		
		List<String> lore = new ArrayList<>(4);
		if (context.getQuest().testTimer(context.getPlayerAccount(), false)) {
			lore.add(Lang.canRedo.toString());
		}else {
			lore.add(Lang.timeWait.format(context.getQuest().getTimeLeft(context.getPlayerAccount())));
		}
		lore.add(null);
		lore.add(Lang.timesFinished.format(context.getQuestDatas().getTimesFinished()));
		return lore;
	}
	
	@Override
	public double getDescriptionPriority() {
		return 100;
	}
	
}
