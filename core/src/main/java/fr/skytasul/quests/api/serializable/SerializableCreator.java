package fr.skytasul.quests.api.serializable;

import java.util.function.Supplier;

public class SerializableCreator<T extends SerializableObject> {
	
	private final String id;
	private final Class<? extends T> clazz;
	private final Supplier<T> newObjectSupplier;

	/**
	 * Creates a new creator for the serializable object of type <code>&lt;T&gt;</code>
	 * @param id unique string id for the serializable object type
	 * @param clazz class of the serializable object type
	 * @param newObjectSupplier function used to instanciate a serializable object
	 */
	public SerializableCreator(String id, Class<? extends T> clazz, Supplier<T> newObjectSupplier) {
		this.id = id;
		this.clazz = clazz;
		this.newObjectSupplier = newObjectSupplier;
	}
	
	public String getID() {
		return id;
	}
	
	public Class<? extends T> getSerializableClass() {
		return clazz;
	}
	
	public T newObject() {
		return newObjectSupplier.get();
	}
	
}