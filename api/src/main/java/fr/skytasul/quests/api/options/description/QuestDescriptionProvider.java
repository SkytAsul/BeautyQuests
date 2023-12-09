package fr.skytasul.quests.api.options.description;

import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface QuestDescriptionProvider {
	
	public static final Comparator<QuestDescriptionProvider> COMPARATOR = Comparator.comparingDouble(QuestDescriptionProvider::getDescriptionPriority);
	
	@Nullable
	List<@Nullable String> provideDescription(@NotNull QuestDescriptionContext context);
	
	@NotNull
	String getDescriptionId();

	double getDescriptionPriority();
	
	default boolean prefixDescriptionWithNewLine() {
		return true;
	}
	
}
