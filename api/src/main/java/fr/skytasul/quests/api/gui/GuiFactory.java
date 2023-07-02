package fr.skytasul.quests.api.gui;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.npcs.BQNPC;
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
	Gui createNpcSelection(@NotNull Runnable cancel, @NotNull Consumer<BQNPC> callback, boolean nullable);

}
