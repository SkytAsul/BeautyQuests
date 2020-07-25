package fr.skytasul.quests.api.options;

import java.util.Iterator;

public interface OptionSet {
	
	Iterator<QuestOption> iterator();
	
	<T extends QuestOption<?>> T getOption(Class<T> optionClass);
	
}