package fr.skytasul.quests.api.options.description;

import java.util.Comparator;
import java.util.List;

public interface QuestDescriptionProvider {
	
	public static final Comparator<QuestDescriptionProvider> COMPARATOR = Comparator.comparingDouble(QuestDescriptionProvider::getDescriptionPriority);
	
	List<String> provideDescription(QuestDescriptionContext context);
	
	String getDescriptionId();

	double getDescriptionPriority();
	
	default boolean prefixDescriptionWithNewLine() {
		return true;
	}
	
}
