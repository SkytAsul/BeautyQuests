package fr.skytasul.quests.api.objects;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;

public class QuestObjectCreator<T extends QuestObject> extends SerializableCreator<T> {
	
	private final ItemStack item;
	private final boolean multiple;
	private QuestObjectLocation[] allowedLocations;
	
	/**
	 * @param id unique identifier for the object
	 * @param clazz Class extending {@link T}
	 * @param item ItemStack shown in {@link QuestObjectGUI}
	 * @param newObjectSupplier lambda returning an instance of this Object ({@link T}::new)
	 */
	public QuestObjectCreator(String id, Class<? extends T> clazz, ItemStack item, Supplier<T> newObjectSupplier) {
		this(id, clazz, item, newObjectSupplier, true);
	}
	
	/**
	 * @param id unique identifier for the object
	 * @param clazz Class extending {@link T}
	 * @param item ItemStack shown in {@link QuestObjectGUI}
	 * @param newObjectSupplier lambda returning an instance of this Object ({@link T}::new)
	 * @param multiple can the object be present multiple times
	 * @param allowedLocations if present, specifies where the object can be used.
	 * If no location is specified, then all locations are accepted.
	 */
	public QuestObjectCreator(String id, Class<? extends T> clazz, ItemStack item, Supplier<T> newObjectSupplier, boolean multiple, QuestObjectLocation... allowedLocations) {
		super(id, clazz, newObjectSupplier);
		this.item = item;
		this.multiple = multiple;
		this.allowedLocations = allowedLocations;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public boolean canBeMultiple() {
		return multiple;
	}
	
	public boolean isAllowed(QuestObjectLocation location) {
		if (allowedLocations.length == 0) return true;
		for (QuestObjectLocation allowed : allowedLocations) {
			if (allowed == location) return true;
		}
		return false;
	}
	
}
