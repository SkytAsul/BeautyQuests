package fr.skytasul.quests.api.utils.messaging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MessageProcessor extends Comparable<MessageProcessor> {

	default @Nullable PlaceholderRegistry processPlaceholders(@Nullable PlaceholderRegistry placeholders,
			@NotNull PlaceholdersContext context) {
		return placeholders;
	}

	default @NotNull String processString(@NotNull String string, @NotNull PlaceholdersContext context) {
		return string;
	}

	int getPriority();

	@Override
	default int compareTo(MessageProcessor o) {
		return Integer.compare(getPriority(), o.getPriority());
	}

}
