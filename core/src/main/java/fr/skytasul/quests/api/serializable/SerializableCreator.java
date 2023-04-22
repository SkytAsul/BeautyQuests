package fr.skytasul.quests.api.serializable;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class SerializableCreator<T extends SerializableObject> {

	private final @NotNull String id;
	private final @NotNull Class<? extends T> clazz;
	private final @NotNull Supplier<@NotNull T> newObjectSupplier;

	/**
	 * Creates a new creator for the serializable object of type <code>&lt;T&gt;</code>
	 * 
	 * @param id unique string id for the serializable object type
	 * @param clazz class of the serializable object type
	 * @param newObjectSupplier function used to instanciate a serializable object
	 */
	public SerializableCreator(@NotNull String id, @NotNull Class<? extends T> clazz,
			Supplier<@NotNull T> newObjectSupplier) {
		this.id = id;
		this.clazz = clazz;
		this.newObjectSupplier = newObjectSupplier;
	}

	public @NotNull String getID() {
		return id;
	}

	public @NotNull Class<? extends T> getSerializableClass() {
		return clazz;
	}

	public @NotNull T newObject() {
		return newObjectSupplier.get();
	}

}
