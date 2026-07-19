package fr.skytasul.quests.api.editors.parsers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AbstractParser<T> {

	@NotNull T parse(@NotNull String string) throws ParsingError;
	
	default @Nullable String getIndication() {
		return null;
	}
	
	class ParsingError extends Exception {

		private static final long serialVersionUID = 1L;

		public ParsingError(@NotNull String message) {
			super(message);
		}

	}

}
