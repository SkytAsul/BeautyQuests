package fr.skytasul.quests.gui.creation;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.misc.ItemCreatorGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class ItemsGUI implements CustomInventory{
	
	public static ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.itemsNone.toString());
	
	private List<ItemStack> items;
	
	public Inventory inv;
	private Consumer<List<ItemStack>> end;
	
	public ItemsGUI(Consumer<List<ItemStack>> end, List<ItemStack> items) {
		this.end = end;
		this.items = Utils.extactItems(items);
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_ITEMS.toString());
		
		inv.setItem(8, ItemUtils.itemDone);
		
		for (int i = 0; i < 8; i++) {
			if (i < items.size()) {
				inv.setItem(i, items.get(i));
			}else inv.setItem(i, none);
		}
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		if (slot == 8) return true;
		if (none.equals(current)){
			inv.setItem(slot, cursor);
			Utils.runSync(() -> p.setItemOnCursor(null)); // AIR ?
			return true;
		}
		return false;
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == 8){
			items.clear();
			for (int i = 0; i < 8; i++){
				ItemStack is = inv.getItem(i);
				if (is != null && !is.equals(none)) items.add(inv.getItem(i));
			}
			Inventories.closeAndExit(p);
			end.accept(items);
		}else {
			if (current.equals(none)){
				Inventories.create(p, new ItemCreatorGUI(item -> {
					if (item != null) inv.setItem(slot, item);
					Inventories.put(p, openLastInv(p), inv);
				}, true));
			}else {
				Utils.runSync(() -> inv.setItem(slot, none));
				return false;
			}
		}
		return true;
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REOPEN;
	}

}