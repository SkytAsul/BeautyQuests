package fr.skytasul.quests.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;

public abstract class Gui {

	private @Nullable Inventory inventory;

	public @Nullable Inventory getInventory() {
		return inventory;
	}

	/**
	 * Called internally when opening inventory
	 * 
	 * @param p Player to open
	 * @return inventory opened
	 */
	public final void show(Player player) {
		if (inventory == null) {
			inventory = instanciate(player);
			populate(player, inventory);
		} else {
			refresh(player, inventory);
		}
		inventory = player.openInventory(inventory).getTopInventory();
	}

	/**
	 * Opens the inventory to the player. Direct reference to
	 * {@link GuiManager#open(Player, CustomInventory)}
	 * 
	 * @param player Player
	 * @see Inventories#create(Player, CustomInventory)
	 */
	public final void open(@NotNull Player player) {
		QuestsPlugin.getPlugin().getGuiManager().open(player, this);
	}
	
	public final void reopen(@NotNull Player player) {
		reopen(player, false);
	}

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

	public final void close(@NotNull Player player) {
		QuestsPlugin.getPlugin().getGuiManager().closeAndExit(player);
	}

	protected abstract Inventory instanciate(@NotNull Player player);

	protected abstract void populate(@NotNull Player player, @NotNull Inventory inventory);

	protected void refresh(@NotNull Player player, @NotNull Inventory inventory) {}

	/**
	 * Called when clicking on an item
	 * 
	 * @param event object containing informations about click action
	 */
	public abstract void onClick(@NotNull GuiClickEvent event);
	
	/**
	 * Called when closing the inventory
	 * 
	 * @param player Player who has the inventory opened
	 * @return Remove player from inventories system
	 */
	public @NotNull CloseBehavior onClose(@NotNull Player player) {
		return StandardCloseBehavior.CONFIRM;
	}
	
}
