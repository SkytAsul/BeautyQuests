package fr.skytasul.quests.gui;

import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiFactory;
import fr.skytasul.quests.api.gui.templates.ConfirmGUI;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.items.ItemComparisonGUI;
import fr.skytasul.quests.gui.items.ItemCreatorGUI;
import fr.skytasul.quests.gui.items.ItemGUI;
import fr.skytasul.quests.gui.items.ItemsGUI;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.gui.npc.NpcSelectGUI;
import fr.skytasul.quests.gui.quests.ChoosePlayerQuestGUI;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.players.PlayerAccountImplementation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DefaultGuiFactory implements GuiFactory {

	@Override
	public @NotNull Gui createPlayerQuestsMenu(@NotNull Quester account) {
		return new PlayerListGUI((PlayerAccountImplementation) account);
	}

	@Override
	public @NotNull Gui createItemSelection(@NotNull Consumer<ItemStack> callback, boolean allowCancel) {
		return new ItemGUI(callback, allowCancel);
	}

	@Override
	public @NotNull Gui createItemCreator(@NotNull Consumer<ItemStack> callback, boolean allowCancel) {
		return new ItemCreatorGUI(callback, allowCancel);
	}

	@Override
	public @NotNull Gui createItemsSelection(@NotNull Consumer<@NotNull List<@NotNull ItemStack>> callback,
			@Nullable List<@Nullable ItemStack> existingItems) {
		return new ItemsGUI(callback, existingItems);
	}

	@Override
	public @NotNull Gui createItemComparisonsSelection(@NotNull ItemComparisonMap comparisons, @NotNull Runnable validate) {
		return new ItemComparisonGUI(comparisons, validate);
	}

	@Override
	public @NotNull Gui createBlocksSelection(@NotNull Consumer<List<MutableCountableObject<BQBlock>>> callback,
			@NotNull Collection<MutableCountableObject<BQBlock>> existingBlocks) {
		return new BlocksGUI(existingBlocks, callback);
	}

	@Override
	public @NotNull Gui createNpcSelection(@NotNull Runnable cancel, @NotNull Consumer<BqNpc> callback,
			boolean nullable) {
		return NpcSelectGUI.select(cancel, callback, nullable);
	}

	@Override
	public @NotNull Gui createConfirmation(@Nullable Runnable yes, @Nullable Runnable no, @NotNull String indication,
			@Nullable List<@Nullable String> lore) {
		return ConfirmGUI.confirm(yes, no, indication, lore);
	}

	@Override
	public @NotNull Gui createEntityTypeSelection(@NotNull Consumer<EntityType> callback,
			@Nullable Predicate<@NotNull EntityType> filter) {
		return new EntityTypeGUI(callback, filter);
	}

	@Override
	public @NotNull Gui createQuestSelection(@NotNull Consumer<Quest> callback, @Nullable Runnable cancel,
			@NotNull Collection<Quest> quests) {
		return new ChooseQuestGUI(quests, callback, cancel);
	}

	@Override
	public @NotNull Gui createPlayerQuestSelection(@NotNull Player player, @NotNull Collection<Quest> quests) {
		return new ChoosePlayerQuestGUI(quests, player);
	}

}
