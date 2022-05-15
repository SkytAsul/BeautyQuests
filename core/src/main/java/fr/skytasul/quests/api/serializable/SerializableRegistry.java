package fr.skytasul.quests.api.serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.skytasul.quests.utils.DebugUtils;

public class SerializableRegistry<T extends SerializableObject, C extends SerializableCreator<T>> implements Iterable<C> {
	
	protected final String id;
	protected final List<C> creators = new ArrayList<>();

	public SerializableRegistry(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
	
	public void register(C creator) {
		if (creators.stream().anyMatch(x -> x.getID().equals(creator.getID())))
			throw new IllegalStateException("A creator with the same id " + creator.getID() + " has been registered.");
		creators.add(creator);
		DebugUtils.logMessage("Quest object registered in registry " + id + " (id: " + creator.getID() + ", class: " + creator.getSerializableClass().getName() + ")");
	}

	public SerializableCreator<T> getByClass(Class<?> clazz) {
		return creators
				.stream()
				.filter(creator -> creator.getSerializableClass().equals(clazz))
				.findAny()
				.orElse(null);
	}

	public SerializableCreator<T> getByID(String id) {
		return creators
				.stream()
				.filter(creator -> creator.getID().equals(id))
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
	
}