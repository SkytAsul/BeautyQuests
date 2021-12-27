package fr.skytasul.quests.api.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.DebugUtils;

public class QuestObjectsRegistry<T extends QuestObject> implements Iterable<QuestObjectCreator<T>> {
	
	private final List<QuestObjectCreator<T>> creators = new ArrayList<>();
	private final String inventoryName;
	
	public QuestObjectsRegistry(String inventoryName) {
		this.inventoryName = inventoryName;
	}
	
	public void register(QuestObjectCreator<? extends T> creator) {
		if (creators.stream().anyMatch(x -> x.id.equals(creator.id)))
			throw new IllegalStateException("A creator with the same id " + creator.id + " has been registered.");
		creators.add((QuestObjectCreator<T>) creator);
		DebugUtils.logMessage("Quest object registered (id: " + creator.id + ", class: " + creator.clazz.getName() + ")");
	}
	
	public QuestObjectCreator<T> getByClass(Class<T> clazz) {
		return creators
				.stream()
				.filter(creator -> creator.clazz.equals(clazz))
				.findAny()
				.orElse(null);
	}
	
	public QuestObjectCreator<T> getByID(String id) {
		return creators
				.stream()
				.filter(creator -> creator.id.equals(id))
				.findAny()
				.orElse(null);
	}
	
	public List<QuestObjectCreator<T>> getCreators() {
		return creators;
	}
	
	@Override
	public Iterator<QuestObjectCreator<T>> iterator() {
		return creators.iterator();
	}
	
	public QuestObjectGUI<T> createGUI(QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return new QuestObjectGUI<>(inventoryName, location, creators, end, objects);
	}
	
}
