package fr.skytasul.quests.api.options.description;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Comparator;
import java.util.List;

// TODO rework this, have id/priority/newline parameters stored in registry
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
