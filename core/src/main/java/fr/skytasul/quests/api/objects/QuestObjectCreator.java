package fr.skytasul.quests.api.objects;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

public class QuestObjectCreator<T extends QuestObject> {
	
	public final Class<T> clazz;
	public final ItemStack item;
	public final Supplier<T> newObjectSupplier;
	public final boolean multiple;
	private QuestObjectLocation[] allowedLocations;
	
	public QuestObjectCreator(Class<T> clazz, ItemStack is, Supplier<T> newObjectSupplier, boolean multiple, QuestObjectLocation... allowedLocations) {
		this.clazz = clazz;
		this.item = is;
		this.newObjectSupplier = newObjectSupplier;
		this.multiple = multiple;
		this.allowedLocations = allowedLocations;
	}
	
	public boolean isAllowed(QuestObjectLocation location) {
		if (allowedLocations.length == 0) return true;
		for (QuestObjectLocation allowed : allowedLocations) {
			if (allowed == location) return true;
		}
		return false;
	}
	
}
