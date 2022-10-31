package fr.skytasul.quests.options;

import java.util.Arrays;
import java.util.List;

import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.gui.quests.PlayerListGUI.Category;
import fr.skytasul.quests.utils.Lang;

public class OptionStartable extends QuestOptionBoolean implements QuestDescriptionProvider {
	
	private static final List<String> STARTABLE = Arrays.asList(Lang.startLore.toString());
	private static final List<String> NOT_STARTABLE = Arrays.asList(Lang.startImpossibleLore.toString());
	
	@Override
	public String getName() {
		return Lang.startableFromGUI.toString();
	}
	
	@Override
	public String getDescription() {
		return Lang.startableFromGUILore.toString();
	}
	
	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		if (context.getCategory() != Category.NOT_STARTED || !context.getPlayerAccount().isCurrent()) return null;
		return context.getQuest().isLauncheable(context.getPlayerAccount().getPlayer(), context.getPlayerAccount(), false) ? STARTABLE : NOT_STARTABLE;
	}
	
	@Override
	public double getDescriptionPriority() {
		return 100;
	}
	
}
