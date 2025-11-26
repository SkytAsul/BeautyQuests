package fr.skytasul.quests.api.gui;

import com.cryptomorin.xseries.inventory.XInventoryView;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGui implements Gui {

	private @Nullable Inventory inventory;

	@Override
	public @Nullable Inventory getInventory() {
		return inventory;
	}

	@Override
	public final void showInternal(Player player) {
		if (inventory == null) {
			createInventory(player);
		} else {
			refreshInternal(player);
		}
		inventory = XInventoryView.of(player.openInventory(inventory)).getTopInventory();
	}

	@Override
	public final void refresh(@NotNull Player player) {
		if (inventory == null)
			return;
		refreshInternal(player);
	}

	protected void createInventory(Player player) {
		inventory = instanciate(player);
		populate(player, inventory);
	}

	protected abstract Inventory instanciate(@NotNull Player player);

	protected abstract void populate(@NotNull Player player, @NotNull Inventory inventory);

	protected void refreshInternal(@NotNull Player player) {
		if (inventory == null)
			inventory = instanciate(player);
		else
			inventory.clear();

		populate(player, inventory);
	}

	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return StandardCloseBehavior.CONFIRM;
	}

}
