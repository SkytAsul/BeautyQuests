package fr.skytasul.quests.api.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.DebugUtils;

public class QuestObjectsRegistry<T extends QuestObject, C extends QuestObjectCreator<T>> implements Iterable<C> {
	
	private final List<C> creators = new ArrayList<>();
	private final String inventoryName;
	
	public QuestObjectsRegistry(String inventoryName) {
		this.inventoryName = inventoryName;
	}
	
	public void register(C creator) {
		if (creators.stream().anyMatch(x -> x.id.equals(creator.id)))
			throw new IllegalStateException("A creator with the same id " + creator.id + " has been registered.");
		creators.add(creator);
		DebugUtils.logMessage("Quest object registered (id: " + creator.id + ", class: " + creator.clazz.getName() + ")");
	}
	
	public C getByClass(Class<?> clazz) {
		return creators
				.stream()
				.filter(creator -> creator.clazz.equals(clazz))
				.findAny()
				.orElse(null);
	}
	
	public C getByID(String id) {
		return creators
				.stream()
				.filter(creator -> creator.id.equals(id))
				.findAny()
				.orElse(null);
	}
	
	public List<C> getCreators() {
		return creators;
	}
	
	@Override
	public Iterator<C> iterator() {
		return creators.iterator();
	}
	
	public QuestObjectGUI<T> createGUI(QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return createGUI(inventoryName, location, end, objects);
	}
	
	public QuestObjectGUI<T> createGUI(String name, QuestObjectLocation location, Consumer<List<T>> end, List<T> objects) {
		return new QuestObjectGUI<>(name, location, (Collection<QuestObjectCreator<T>>) creators, end, objects);
	}
	
}
