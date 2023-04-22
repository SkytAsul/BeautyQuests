package fr.skytasul.quests.api.objects;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.serializable.SerializableRegistry;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;

public class QuestObjectsRegistry<T extends QuestObject, C extends QuestObjectCreator<T>> extends SerializableRegistry<T, C> {
	
	private final @NotNull String inventoryName;
	
	public QuestObjectsRegistry(@NotNull String id, @NotNull String inventoryName) {
		super(id);
		this.inventoryName = inventoryName;
	}
	
	public @NotNull String getInventoryName() {
		return inventoryName;
	}
	
	public QuestObjectGUI<T> createGUI(@NotNull QuestObjectLocation location, @NotNull Consumer<@NotNull List<T>> end,
			@NotNull List<T> objects) {
		return createGUI(inventoryName, location, end, objects, null);
	}
	
	public QuestObjectGUI<T> createGUI(@NotNull QuestObjectLocation location, @NotNull Consumer<@NotNull List<T>> end,
			@NotNull List<T> objects, @Nullable Predicate<C> filter) {
		return createGUI(inventoryName, location, end, objects, filter);
	}
	
	public QuestObjectGUI<T> createGUI(@NotNull String name, @NotNull QuestObjectLocation location,
			@NotNull Consumer<@NotNull List<T>> end, @NotNull List<T> objects) {
		return createGUI(name, location, end, objects, null);
	}
	
	public QuestObjectGUI<T> createGUI(@NotNull String name, @NotNull QuestObjectLocation location,
			@NotNull Consumer<@NotNull List<T>> end, @NotNull List<@NotNull T> objects, @Nullable Predicate<C> filter) {
		return new QuestObjectGUI<>(name, location, (Collection<QuestObjectCreator<T>>) (filter == null ? creators : creators.stream().filter(filter).collect(Collectors.toList())), end, objects);
	}
	
}
