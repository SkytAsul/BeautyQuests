package fr.skytasul.quests.api.options;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuestOptionCreator<D, T extends QuestOption<D>> {
	
	public static Map<Class<? extends QuestOption<?>>, QuestOptionCreator<?, ?>> creators = new HashMap<>();
	
	public final String id;
	public final Class<T> optionClass;
	public final Supplier<T> optionSupplier;
	public final String[] oldNames;
	public final D defaultValue;
	
	public int slot = -1;
	
	public QuestOptionCreator(@NotNull String id, int preferedSlot, @NotNull Class<T> optionClass,
			@NotNull Supplier<@NotNull T> optionSupplier, @Nullable D defaultValue, @NotNull String @NotNull... oldNames) {
		this.id = id;
		this.optionClass = optionClass;
		this.optionSupplier = optionSupplier;
		this.oldNames = oldNames;
		this.defaultValue = defaultValue;
		this.slot = calculateSlot(preferedSlot);
	}
	
	public static int calculateSlot(int preferedSlot) {
		if (creators.size() == 0) return preferedSlot;
		int[] slots = creators.values().stream().mapToInt(creator -> creator.slot).sorted().toArray();
		int prevSlot = -1;
		for (int slot : slots) {
			if (slot >= preferedSlot) {
				if (prevSlot != -1 && prevSlot < preferedSlot) break;
			}
			prevSlot = slot;
		}
		return prevSlot == preferedSlot ? prevSlot + 1 : preferedSlot;
	}
	
	public static int getLastSlot() {
		return creators.values().stream().mapToInt(creator -> creator.slot).max().getAsInt();
	}

	public boolean applies(@NotNull String key) {
		return id.equals(key) || Arrays.stream(oldNames).anyMatch(key::equals);
	}
	
}
