package fr.skytasul.quests.api.quests.quester;

import fr.skytasul.quests.api.serializable.SerializableCreator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.function.Supplier;

public class QuestQuesterStrategyCreator extends SerializableCreator<QuestQuesterStrategy> {

	private final @NotNull ItemStack item;

	public QuestQuesterStrategyCreator(@NotNull String id, @NotNull Class<? extends QuestQuesterStrategy> clazz,
			Supplier<@NotNull QuestQuesterStrategy> newObjectSupplier, @NotNull ItemStack item) {
		super(id, clazz, newObjectSupplier);
		this.item = item;
	}

	public @NotNull ItemStack getItem() {
		return item;
	}

}
