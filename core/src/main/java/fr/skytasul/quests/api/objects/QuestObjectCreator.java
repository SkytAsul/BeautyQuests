package fr.skytasul.quests.api.objects;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.creation.QuestObjectGUI;

public class QuestObjectCreator<T extends QuestObject> {
	
	public final Class<T> clazz;
	public final ItemStack item;
	public final Supplier<T> newObjectSupplier;
	public final boolean multiple;
	private QuestObjectLocation[] allowedLocations;
	
	/**
	 * @param clazz Class extending {@link T}
	 * @param item ItemStack shown in {@link QuestObjectGUI}
	 * @param newObjectSupplier lambda returning an instance of this Object ({@link T}::new)
	 */
	public QuestObjectCreator(Class<T> clazz, ItemStack is, Supplier<T> newObjectSupplier) {
		this(clazz, is, newObjectSupplier, true);
	}
	
	/**
	 * @param clazz Class extending {@link T}
	 * @param item ItemStack shown in {@link QuestObjectGUI}
	 * @param newObjectSupplier lambda returning an instance of this Object ({@link T}::new)
	 * @param multiple can the object be present multiple times
	 * @param allowedLocations if present, specifies where the object can be used
	 */
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
