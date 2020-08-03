package fr.skytasul.quests.api.objects;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

public class QuestObjectCreator<T extends QuestObject> {
	
	public final Class<T> clazz;
	public final ItemStack item;
	public final Supplier<T> newObjectSupplier;
	
	public QuestObjectCreator(Class<T> clazz, ItemStack is, Supplier<T> newObjectSupplier) {
		this.clazz = clazz;
		this.item = is;
		this.newObjectSupplier = newObjectSupplier;
	}
	
}
