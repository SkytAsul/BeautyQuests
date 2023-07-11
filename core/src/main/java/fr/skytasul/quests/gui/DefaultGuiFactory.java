package fr.skytasul.quests.gui;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiFactory;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.items.ItemCreatorGUI;
import fr.skytasul.quests.gui.items.ItemGUI;
import fr.skytasul.quests.gui.npc.NpcSelectGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.players.PlayerAccountImplementation;

public class DefaultGuiFactory implements GuiFactory {

	@Override
	public @NotNull Gui createPlayerQuestsMenu(@NotNull PlayerAccount account) {
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
	public @NotNull Gui createBlocksSelection(@NotNull Consumer<List<MutableCountableObject<BQBlock>>> callback,
			@NotNull Collection<MutableCountableObject<BQBlock>> existingBlocks) {
		return new BlocksGUI(existingBlocks, callback);
	}

	@Override
	public @NotNull Gui createNpcSelection(@NotNull Runnable cancel, @NotNull Consumer<BqNpc> callback,
			boolean nullable) {
		return NpcSelectGUI.select(cancel, callback, nullable);
	}

}
