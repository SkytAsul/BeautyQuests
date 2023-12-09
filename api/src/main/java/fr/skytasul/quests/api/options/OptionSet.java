package fr.skytasul.quests.api.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings ("rawtypes")
public interface OptionSet extends Iterable<QuestOption> {

	<T extends QuestOption<?>> @NotNull T getOption(@NotNull Class<T> optionClass);
	
	boolean hasOption(@NotNull Class<? extends QuestOption<?>> clazz);
	
	default @Nullable <D> D getOptionValueOrDef(@NotNull Class<? extends QuestOption<D>> clazz) {
		for (QuestOption<?> option : this) {
			if (clazz.isInstance(option))
				return (D) option.getValue();
		}
		return (D) QuestOptionCreator.creators.get(clazz).defaultValue;
	}

}