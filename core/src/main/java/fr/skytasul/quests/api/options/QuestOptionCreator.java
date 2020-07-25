package fr.skytasul.quests.api.options;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class QuestOptionCreator<D, T extends QuestOption<D>> {
	
	public static Map<Class<? extends QuestOption<?>>, QuestOptionCreator<?, ?>> creators = new HashMap<>();
	
	public final String id;
	public final Class<T> optionClass;
	public final Supplier<T> optionSupplier;
	public final String[] oldNames;
	public final D defaultValue;
	
	public int slot = -1;
	
	public QuestOptionCreator(String id, int preferedSlot, Class<T> optionClass, Supplier<T> optionSupplier, D defaultValue, String... oldNames) {
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
	
	public boolean applies(String key) {
		return id.equals(key) || Arrays.stream(oldNames).anyMatch(key::equals);
	}
	
}
