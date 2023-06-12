package fr.skytasul.quests.api.quests.creation;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;

public interface QuestCreationGui extends Gui {

	void updateOptionItem(@NotNull QuestOption<?> option);

	@NotNull
	OptionSet getOptionSet();

}
