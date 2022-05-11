package fr.skytasul.quests.api.serializable;

import java.util.function.Supplier;

public class SerializableCreator<T extends SerializableObject> {
	
	public final String id;
	public final Class<? extends T> clazz;
	public final Supplier<T> newObjectSupplier;

	public SerializableCreator(String id, Class<? extends T> clazz, Supplier<T> newObjectSupplier) {
		this.id = id;
		this.clazz = clazz;
		this.newObjectSupplier = newObjectSupplier;
	}
	
}