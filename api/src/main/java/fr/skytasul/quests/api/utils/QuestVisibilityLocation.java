package fr.skytasul.quests.api.utils;

import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.localization.Lang;

public enum QuestVisibilityLocation {

	TAB_NOT_STARTED(Lang.visibility_notStarted.toString()),
	TAB_IN_PROGRESS(Lang.visibility_inProgress.toString()),
	TAB_FINISHED(Lang.visibility_finished.toString()),
	MAPS(Lang.visibility_maps.toString());
	
	private final @NotNull String name;
	
	private QuestVisibilityLocation(@NotNull String name) {
		this.name = name;
	}
	
	public @NotNull String getName() {
		return name;
	}
	
}