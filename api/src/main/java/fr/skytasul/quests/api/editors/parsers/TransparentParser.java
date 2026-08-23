package fr.skytasul.quests.api.editors.parsers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransparentParser implements AbstractParser<String> {

	@Override
	public @NotNull String parse(@NotNull String string) {
		return string;
	}

	@Override
	public @Nullable String serialize(@NotNull String value) {
		return value;
	}

}
