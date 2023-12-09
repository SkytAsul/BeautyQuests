package fr.skytasul.quests.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.close.CloseBehavior;

public interface Gui {

	/**
	 * Gets the inventory for this Gui.
	 * 
	 * @return the Bukkit inventory of this Gui, or <code>null</code> if it has not yet been built.
	 */
	@Nullable
	Inventory getInventory();

	/**
	 * Called internally when opening inventory
	 * 
	 * @param p Player to open
	 * @return inventory opened
	 */
	void showInternal(@NotNull Player player);
	
	/**
	 * Opens the inventory to the player. Direct reference to
	 * {@link GuiManager#open(Player, CustomInventory)}
	 * 
	 * @param player Player
	 * @see Inventories#create(Player, CustomInventory)
	 */
	default void open(@NotNull Player player) {
		QuestsPlugin.getPlugin().getGuiManager().open(player, this);
	}

	default void reopen(@NotNull Player player) {
		reopen(player, false);
	}

	void reopen(@NotNull Player player, boolean refresh);

	default void close(@NotNull Player player) {
		QuestsPlugin.getPlugin().getGuiManager().closeAndExit(player);
	}

	/**
	 * Called when clicking on an item
	 * 
	 * @param event object containing informations about click action
	 */
	void onClick(@NotNull GuiClickEvent event);

	/**
	 * Called when closing the inventory
	 * 
	 * @param player Player who has the inventory opened
	 * @return behavior to have for this closing
	 */
	@NotNull
	CloseBehavior onClose(@NotNull Player player);

}
