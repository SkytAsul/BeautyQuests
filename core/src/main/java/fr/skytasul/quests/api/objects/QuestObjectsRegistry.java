package fr.skytasul.quests.api.objects;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import fr.skytasul.quests.api.serializable.SerializableRegistry;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;

public class QuestObjectsRegistry<T extends QuestObject, C extends QuestObjectCreator<T>> extends SerializableRegistry<T, C> {
	
	private final String inventoryName;
	
	public QuestObjectsRegistry(String inventoryName) {
		this.inventoryName = inventoryName;
	}
	
	public QuestObjectGUI<T> createGUI(QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return createGUI(inventoryName, location, end, objects);
	}
	
	public QuestObjectGUI<T> createGUI(String name, QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return new QuestObjectGUI<>(name, location, (Collection<QuestObjectCreator<T>>) creators, end, objects);
	}
	
}
