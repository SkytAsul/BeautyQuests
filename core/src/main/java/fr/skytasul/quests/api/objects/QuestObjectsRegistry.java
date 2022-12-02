package fr.skytasul.quests.api.objects;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.skytasul.quests.api.serializable.SerializableRegistry;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;

public class QuestObjectsRegistry<T extends QuestObject, C extends QuestObjectCreator<T>> extends SerializableRegistry<T, C> {
	
	private final String inventoryName;
	
	public QuestObjectsRegistry(String id, String inventoryName) {
		super(id);
		this.inventoryName = inventoryName;
	}
	
	public String getInventoryName() {
		return inventoryName;
	}
	
	public QuestObjectGUI<T> createGUI(QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return createGUI(inventoryName, location, end, objects, null);
	}
	
	public QuestObjectGUI<T> createGUI(QuestObjectLocation location, Consumer<List<T>> end, List<T> objects, Predicate<C> filter) {
		return createGUI(inventoryName, location, end, objects, filter);
	}
	
	public QuestObjectGUI<T> createGUI(String name, QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return createGUI(name, location, end, objects, null);
	}
	
	public QuestObjectGUI<T> createGUI(String name, QuestObjectLocation location, Consumer<List<T>> end, List<T> objects, Predicate<C> filter) {
		return new QuestObjectGUI<>(name, location, (Collection<QuestObjectCreator<T>>) (filter == null ? creators : creators.stream().filter(filter).collect(Collectors.toList())), end, objects);
	}
	
}
