package fr.skytasul.quests.api.utils.messaging;

import org.jetbrains.annotations.NotNull;

public interface HasPlaceholders {

	@NotNull
	PlaceholderRegistry getPlaceholdersRegistry();

}
