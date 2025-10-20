package fr.skytasul.quests.gui.items;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.utils.QuestUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class ItemGUI extends AbstractGui {

	private Consumer<ItemStack> end;
	private boolean allowCancel;
	
	public ItemGUI(Consumer<ItemStack> end, boolean allowCancel) {
		this.end = end;
		this.allowCancel = allowCancel;
	}
	
	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, InventoryType.DROPPER, Lang.INVENTORY_ITEM.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		ItemStack separator = ItemUtils.itemSeparator(DyeColor.LIGHT_BLUE);
		for (int i = 0; i < 9; i++){
			if (i == 4){
				inventory.setItem(i, ItemsGUI.none);
			} else
				inventory.setItem(i, separator);
		}
	}

	@Override
	public void onClick(GuiClickEvent event) {
		if (event.getSlot() == 4) {
			if (event.hasCursor()) {
				event.getPlayer().setItemOnCursor(null);
				event.setCancelled(false);
				QuestUtils.runSync(() -> end.accept(event.getCursor()));
			} else {
				QuestsPlugin.getPlugin().getGuiManager().getFactory().createItemCreator(end, false).open(event.getPlayer());
			}
		}
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return allowCancel ? new DelayCloseBehavior(() -> end.accept(null)) : StandardCloseBehavior.REOPEN;
	}

}
