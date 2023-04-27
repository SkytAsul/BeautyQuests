package fr.skytasul.quests.api.gui.layout;

import org.jetbrains.annotations.NotNull;

public interface ClickHandler {

	public static final ClickHandler EMPTY = event -> {};
	
	void click(@NotNull ClickEvent event);

}
