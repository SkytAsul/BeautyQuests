package fr.skytasul.quests.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.GuiFactory;
import fr.skytasul.quests.api.gui.GuiManager;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.close.OpenCloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.utils.QuestUtils;

public class GuiManagerImplementation implements GuiManager, Listener {

	private Map<Player, Gui> players = new HashMap<>();
	private boolean dismissClose = false;
	private GuiFactory factory;

	public GuiManagerImplementation() {
		setFactory(new DefaultGuiFactory());
	}

	@Override
	public void open(@NotNull Player player, @NotNull Gui inventory) {
		try {
			closeWithoutExit(player);
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.debug(player.getName() + " has opened inventory " + inventory.getClass().getName() + ".");
			inventory.showInternal(player);
			players.put(player, inventory);
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe(
					"Cannot open inventory " + inventory.getClass().getSimpleName() + " to player " + player.getName(), ex);
			closeAndExit(player);
		}
	}

	@Override
	public void closeAndExit(@NotNull Player player) {
		players.remove(player);
		player.closeInventory();
	}

	@Override
	public void closeWithoutExit(@NotNull Player player) {
		dismissClose = true;
		player.closeInventory();
		dismissClose = false; // in case the player did not have an inventory opened
	}

	@Override
	public void closeAll() {
		for (Iterator<Player> iterator = players.keySet().iterator(); iterator.hasNext();) {
			Player player = iterator.next();
			iterator.remove();
			player.closeInventory();
		}
	}

	@Override
	public boolean hasGuiOpened(@NotNull Player player) {
		return players.containsKey(player);
	}

	@Override
	public @Nullable Gui getOpenedGui(@NotNull Player player) {
		return players.get(player);
	}

	@Override
	public @NotNull GuiFactory getFactory() {
		return factory;
	}

	@Override
	public void setFactory(@NotNull GuiFactory factory) {
		this.factory = factory;
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if (dismissClose) {
			dismissClose = false;
			return;
		}

		if (!(event.getPlayer() instanceof Player))
			return;
		Player player = (Player) event.getPlayer();

		Gui gui = players.get(player);
		if (gui == null)
			return;

		ensureSameInventory(gui, event.getInventory());

		CloseBehavior behavior = gui.onClose(player);
		if (behavior instanceof StandardCloseBehavior) {
			switch ((StandardCloseBehavior) behavior) {
				case CONFIRM:
					QuestUtils.runSync(() -> factory.createConfirmation(() -> closeAndExit(player), () -> open(player, gui),
							Lang.INDICATION_CLOSE.toString()).open(player));
					break;
				case NOTHING:
					break;
				case REMOVE:
					players.remove(player);
					break;
				case REOPEN:
					QuestUtils.runSync(() -> open(player, gui));
					break;
			}
		} else if (behavior instanceof DelayCloseBehavior) {
			players.remove(player);
			QuestUtils.runSync(((DelayCloseBehavior) behavior).getDelayed());
		} else if (behavior instanceof OpenCloseBehavior) {
			open(player, ((OpenCloseBehavior) behavior).getOther());
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;
		Player player = (Player) event.getWhoClicked();

		Gui gui = players.get(player);
		if (gui == null)
			return;

		event.setCancelled(false);

		try {
			if (event.getClickedInventory() == player.getInventory()) {
				if (event.isShiftClick())
					event.setCancelled(true);
				return;
			}

			ClickType click = event.getClick();
			if (click == ClickType.NUMBER_KEY || click == ClickType.DOUBLE_CLICK || click == ClickType.DROP
					|| click == ClickType.CONTROL_DROP || click.name().equals("SWAP_OFFHAND")) { // SWAP_OFFHAND introduced
																									// in 1.16
				event.setCancelled(true);
				return;
			}

			ensureSameInventory(gui, event.getClickedInventory());

			if (event.getCursor().getType() == Material.AIR
					&& (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR))
				return;

			GuiClickEvent guiEvent = new GuiClickEvent(player, gui, event.getCurrentItem(), event.getCursor(),
					event.getSlot(), event.getClick());
			gui.onClick(guiEvent);
			event.setCancelled(guiEvent.isCancelled());
		} catch (Exception ex) {
			event.setCancelled(true);
			DefaultErrors.sendGeneric(player, ex.getMessage() + " in " + gui.getClass().getSimpleName());
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred when " + player.getName()
					+ " clicked in inventory " + gui.getClass().getName() + " at slot " + event.getSlot(), ex);
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (players.containsKey(event.getWhoClicked()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onOpen(InventoryOpenEvent event) {
		if (!event.isCancelled())
			return;
		if (players.containsKey(event.getPlayer())) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("The opening of a BeautyQuests menu for player "
					+ event.getPlayer().getName() + " has been cancelled by another plugin.");
		}
	}

	private void ensureSameInventory(Gui gui, Inventory inventory) {
		if (gui.getInventory() != inventory)
			throw new IllegalStateException(
					"The inventory opened by the player is not the same as the one registered by the plugin");
	}

}
