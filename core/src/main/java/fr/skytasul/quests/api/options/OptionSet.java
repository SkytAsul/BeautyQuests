package fr.skytasul.quests.api.options;

@SuppressWarnings ("rawtypes")
public interface OptionSet extends Iterable<QuestOption> {

	<T extends QuestOption<?>> T getOption(Class<T> optionClass);
	
	boolean hasOption(Class<? extends QuestOption<?>> clazz);
	
}