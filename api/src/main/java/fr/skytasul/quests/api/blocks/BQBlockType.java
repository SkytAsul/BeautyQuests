package fr.skytasul.quests.api.blocks;

import org.jetbrains.annotations.NotNull;

public interface BQBlockType {

	@NotNull
	BQBlock deserialize(@NotNull String string, @NotNull BQBlockOptions serializedOptions)
			throws IllegalArgumentException;

}
