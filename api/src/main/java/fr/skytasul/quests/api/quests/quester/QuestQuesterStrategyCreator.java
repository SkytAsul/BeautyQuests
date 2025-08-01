package fr.skytasul.quests.api.quests.quester;

import fr.skytasul.quests.api.serializable.SerializableCreator;
import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class QuestQuesterStrategyCreator extends SerializableCreator<QuestQuesterStrategy> {

	private final @NotNull String name, description;

	public QuestQuesterStrategyCreator(@NotNull String id, @NotNull Class<? extends QuestQuesterStrategy> clazz,
			Supplier<@NotNull QuestQuesterStrategy> newObjectSupplier, @NotNull String name, @NotNull String description) {
		super(id, clazz, newObjectSupplier);
		this.name = name;
		this.description = description;
	}

	public @NotNull String getName() {
		return name;
	}

	public @NotNull String getDescription() {
		return description;
	}

}
