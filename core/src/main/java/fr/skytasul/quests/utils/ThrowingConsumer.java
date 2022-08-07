package fr.skytasul.quests.utils;

@FunctionalInterface
public interface ThrowingConsumer<T, E extends Throwable> {
	
	public void accept(T object) throws E;
	
}
