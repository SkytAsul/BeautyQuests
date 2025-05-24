package fr.skytasul.quests.api.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.function.Supplier;

public class QuestOptionCreator<D, T extends QuestOption<D>> {

	public final String id;
	public final int preferedSlot;
	public final Class<T> optionClass;
	public final Supplier<T> optionSupplier;
	public final String[] oldNames;
	public final D defaultValue;

	public QuestOptionCreator(@NotNull String id, int preferedSlot, @NotNull Class<T> optionClass,
			@NotNull Supplier<@NotNull T> optionSupplier, @Nullable D defaultValue, @NotNull String @NotNull... oldNames) {
		this.id = id;
		this.preferedSlot = preferedSlot;
		this.optionClass = optionClass;
		this.optionSupplier = optionSupplier;
		this.oldNames = oldNames;
		this.defaultValue = defaultValue;
	}

	public boolean applies(@NotNull String key) {
		return id.equals(key) || Arrays.stream(oldNames).anyMatch(key::equals);
	}

}
