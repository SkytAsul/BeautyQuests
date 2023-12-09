package fr.skytasul.quests.api.gui.layout;

import org.jetbrains.annotations.NotNull;

public interface LayoutedClickHandler {

	public static final LayoutedClickHandler EMPTY = event -> {};
	
	void click(@NotNull LayoutedClickEvent event);

}
