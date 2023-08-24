package fr.skytasul.quests.api.utils.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MessageProcessor {

	default @Nullable PlaceholderRegistry processPlaceholders(@Nullable PlaceholderRegistry placeholders,
			@NotNull PlaceholdersContext context) {
		return placeholders;
	}

	default @NotNull String processString(@NotNull String string, @NotNull PlaceholdersContext context) {
		return string;
	}

}
