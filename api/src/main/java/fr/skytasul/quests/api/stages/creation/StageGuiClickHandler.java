package fr.skytasul.quests.api.stages.creation;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface StageGuiClickHandler {

	void onClick(@NotNull StageGuiClickEvent event);

}
