package fr.skytasul.quests.api.stages.creation;

import java.util.List;
import java.util.function.UnaryOperator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.gui.ItemUtils;

public interface StageGuiLine {

	@Nullable
	ItemStack getItem(int slot);

	void setItem(int slot, @NotNull ItemStack item, @Nullable StageGuiClickHandler click);

	void refreshItem(int slot, @NotNull ItemStack item);

	default void refreshItem(int slot, UnaryOperator<ItemStack> consumer) {
		refreshItem(slot, consumer.apply(getItem(slot)));
	}

	default void refreshItemLore(int slot, String... lore) {
		refreshItem(slot, ItemUtils.lore(getItem(slot), lore));
	}

	default void refreshItemLore(int slot, List<String> lore) {
		refreshItem(slot, ItemUtils.lore(getItem(slot), lore));
	}

	default void refreshItemName(int slot, String name) {
		refreshItem(slot, ItemUtils.name(getItem(slot), name));
	}

	void removeItem(int slot);

	int getPage();

	void setPage(int page);

	@Nullable
	StageGuiClickHandler getClick(int rawSlot);

}
