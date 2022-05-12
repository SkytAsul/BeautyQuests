package fr.skytasul.quests.api.serializable;

import java.util.function.Supplier;

public class SerializableCreator<T extends SerializableObject> {
	
	private final String id;
	private final Class<? extends T> clazz;
	private final Supplier<T> newObjectSupplier;

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