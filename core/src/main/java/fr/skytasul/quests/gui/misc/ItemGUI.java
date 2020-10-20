package fr.skytasul.quests.gui.misc;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.ItemsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class ItemGUI implements CustomInventory {

	private Consumer<ItemStack> end;
	private Runnable cancel;
	
	public ItemGUI(Consumer<ItemStack> end, Runnable cancel) {
		this.end = end;
		this.cancel = cancel;
	}
	
	public Inventory open(Player p){
		Inventory inv = Bukkit.createInventory(null, InventoryType.DROPPER, Lang.INVENTORY_ITEM.toString());
		
		ItemStack separator = ItemUtils.itemSeparator(DyeColor.LIGHT_BLUE);
		for (int i = 0; i < 9; i++){
			if (i == 4){
				inv.setItem(i, ItemsGUI.none);
			}else inv.setItem(i, separator);
		}
		
		return p.openInventory(inv).getTopInventory();
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot != 4) return true;
		new ItemCreatorGUI((obj) -> {
			end.accept(obj);
		}, false).create(p);
		return true;
	}
	
	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		p.setItemOnCursor(null);
		end.accept(cursor);
		return false;
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(cancel);
		return CloseBehavior.NOTHING;
	}

}
