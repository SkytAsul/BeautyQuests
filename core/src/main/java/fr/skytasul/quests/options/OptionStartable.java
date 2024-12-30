package fr.skytasul.quests.options;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOptionBoolean;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import java.util.Arrays;
import java.util.List;

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
		if (context.getCategory() != PlayerListCategory.NOT_STARTED || context.getPlayer() != null)
			return null;
		return context.getQuest().canStart(context.getPlayer(), false) ? STARTABLE : NOT_STARTABLE;
	}

	@Override
	public String getDescriptionId() {
		return "startable_from_gui";
	}

	@Override
	public double getDescriptionPriority() {
		return 100;
	}

}
