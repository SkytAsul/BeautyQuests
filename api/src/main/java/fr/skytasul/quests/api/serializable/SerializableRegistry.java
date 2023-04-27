package fr.skytasul.quests.api.serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;

/**
 * This class is a registry for types of objects that can be serialized
 * into Spigot configuration system.
 * 
 * @param <T> type of serializable object
 * @param <C> type of the creator associated with the serializable objects
 */
public class SerializableRegistry<T extends SerializableObject, C extends SerializableCreator<T>> implements Iterable<C> {
	
	protected final String id;
	protected final List<C> creators = new ArrayList<>();

	public SerializableRegistry(@NotNull String id) {
		this.id = id;
	}
	
	public @NotNull String getID() {
		return id;
	}
	
	/**
	 * Registers a new type of serializable object.
	 * @param creator object that will be used to instanciate objects of type <code>&lt;T&gt;</code>
	 */
	public void register(@NotNull C creator) {
		if (creators.stream().anyMatch(x -> x.getID().equals(creator.getID())))
			throw new IllegalStateException("A creator with the same id " + creator.getID() + " has been registered.");
		creators.add(creator);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Quest object registered in registry " + id + " (id: " + creator.getID() + ", class: " + creator.getSerializableClass().getName() + ")");
	}

	public @Nullable C getByClass(@NotNull Class<?> clazz) {
		return creators
				.stream()
				.filter(creator -> creator.getSerializableClass().equals(clazz))
				.findAny()
				.orElse(null);
	}

	public @Nullable C getByID(@NotNull String id) {
		return creators
				.stream()
				.filter(creator -> creator.getID().equals(id))
				.findAny()
				.orElse(null);
	}

	public @NotNull List<C> getCreators() {
		return creators;
	}

	@Override
	public Iterator<C> iterator() {
		return creators.iterator();
	}
	
}