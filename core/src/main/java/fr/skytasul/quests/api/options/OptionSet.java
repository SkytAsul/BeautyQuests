package fr.skytasul.quests.api.options;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("rawtypes")
public interface OptionSet extends Iterable<QuestOption> {

	<T extends QuestOption<?>> @NotNull T getOption(@NotNull Class<T> optionClass);
	
	boolean hasOption(@NotNull Class<? extends QuestOption<?>> clazz);
	
}