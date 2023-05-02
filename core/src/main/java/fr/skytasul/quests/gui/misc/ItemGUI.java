package fr.skytasul.quests.gui.misc;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.gui.creation.ItemsGUI;

public class ItemGUI extends Gui {

	private Consumer<ItemStack> end;
	private Runnable cancel;
	
	public ItemGUI(Consumer<ItemStack> end, Runnable cancel) {
		this.end = end;
		this.cancel = cancel;
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
		if (slot != 4) return true;
		new ItemCreatorGUI((obj) -> {
			end.accept(obj);
		}, false).open(p);
		return true;
	}
	
	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		if (slot != 4) return true;
		p.setItemOnCursor(null);
		end.accept(cursor);
		return false;
	}
	
	@Override
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(cancel);
	}

}
