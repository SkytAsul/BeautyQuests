package fr.skytasul.quests.api.gui;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;

public interface GuiFactory {

	@NotNull
	Gui createPlayerQuestsMenu(@NotNull PlayerAccount account);

	@NotNull
	Gui createItemSelection(@NotNull Consumer<ItemStack> callback, boolean allowCancel);

	@NotNull
	default Gui createItemSelection(@NotNull Consumer<ItemStack> callback, Runnable cancel) {
		return createItemSelection(item -> {
			if (item == null)
				cancel.run();
			else
				callback.accept(item);
		}, true);
	}

	@NotNull
	Gui createItemCreator(@NotNull Consumer<ItemStack> callback, boolean allowCancel);

	@NotNull
	Gui createBlocksSelection(@NotNull Consumer<List<MutableCountableObject<BQBlock>>> callback,
			@NotNull Collection<MutableCountableObject<BQBlock>> existingBlocks);

	@NotNull
	Gui createNpcSelection(@NotNull Runnable cancel, @NotNull Consumer<BqNpc> callback, boolean nullable);

	@NotNull
	default Gui createConfirmation(@Nullable Runnable yes, @Nullable Runnable no, @NotNull String indication,
			@NotNull String @Nullable... lore) {
		return createConfirmation(yes, no, indication, Arrays.asList(lore));
	}

	@NotNull
	Gui createConfirmation(@Nullable Runnable yes, @Nullable Runnable no, @NotNull String indication,
			@Nullable List<@Nullable String> lore);

}
