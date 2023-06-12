package fr.skytasul.quests.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;

public abstract class AbstractGui implements Gui {

	private @Nullable Inventory inventory;

	@Override
	public @Nullable Inventory getInventory() {
		return inventory;
	}

	@Override
	public final void showInternal(Player player) {
		if (inventory == null) {
			inventory = instanciate(player);
			populate(player, inventory);
		} else {
			refresh(player, inventory);
		}
		inventory = player.openInventory(inventory).getTopInventory();
	}

	@Override
	public final void reopen(@NotNull Player player, boolean refresh) {
		if (refresh)
			inventory = null;
		open(player);
	}

	public final void repopulate(@NotNull Player player) {
		if (inventory == null)
			return;

		inventory.clear();
		populate(player, inventory);
	}

	protected abstract Inventory instanciate(@NotNull Player player);

	protected abstract void populate(@NotNull Player player, @NotNull Inventory inventory);

	protected void refresh(@NotNull Player player, @NotNull Inventory inventory) {}
	
	@Override
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return StandardCloseBehavior.CONFIRM;
	}
	
}
