package fr.skytasul.quests.api.editors.parsers;

import org.jetbrains.annotations.NotNull;

public class TransparentParser implements AbstractParser<String> {

	@Override
	public @NotNull String parse(@NotNull String string) {
		return string;
	}

}
