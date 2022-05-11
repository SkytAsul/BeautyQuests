package fr.skytasul.quests.api.serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.skytasul.quests.utils.DebugUtils;

public class SerializableRegistry<T extends SerializableObject, C extends SerializableCreator<T>> implements Iterable<C> {
	
	protected final List<C> creators = new ArrayList<>();

	public void register(C creator) {
		if (creators.stream().anyMatch(x -> x.id.equals(creator.id)))
			throw new IllegalStateException("A creator with the same id " + creator.id + " has been registered.");
		creators.add(creator);
		DebugUtils.logMessage("Quest object registered (id: " + creator.id + ", class: " + creator.clazz.getName() + ")");
	}

	public SerializableCreator<T> getByClass(Class<?> clazz) {
		return creators
				.stream()
				.filter(creator -> creator.clazz.equals(clazz))
				.findAny()
				.orElse(null);
	}

	public SerializableCreator<T> getByID(String id) {
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
	
}