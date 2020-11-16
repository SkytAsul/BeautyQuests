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

public class ItemsGUI implements CustomInventory {
	
	public static ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.itemsNone.toString());
	
	private Consumer<List<ItemStack>> end;
	private List<ItemStack> items;
	private int size;
	
	public Inventory inv;
	
	public ItemsGUI(Consumer<List<ItemStack>> end, List<ItemStack> items) {
		this.end = end;
		this.items = Utils.extractItems(items);
		this.size = (int) (Math.ceil((items.size() + 1D) / 9D) * 9);
	}

	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, size, Lang.INVENTORY_ITEMS.toString());
		
		inv.setItem(size - 1, ItemUtils.itemDone);
		
		for (int i = 0; i < size - 1; i++) {
			if (i < items.size()) {
				inv.setItem(i, items.get(i));
			}else inv.setItem(i, none);
		}
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	private boolean addItem(Player p) {
		items.clear();
		for (int i = 0; i < size - 1; i++) {
			ItemStack is = inv.getItem(i);
			if (is.equals(none)) return false;
			items.add(is);
		}
		size += 9;
		Inventories.put(p, this, open(p));
		return true;
	}

	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){
		if (slot == size - 1) return true;
		if (none.equals(current)){
			inv.setItem(slot, cursor);
			Utils.runSync(() -> {
				p.setItemOnCursor(null);
				addItem(p);
			});
			return true;
		}
		return false;
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == size - 1) {
			items.clear();
			for (int i = 0; i < size - 1; i++) {
				ItemStack is = inv.getItem(i);
				if (is != null && !is.equals(none)) items.add(inv.getItem(i));
			}
			Inventories.closeAndExit(p);
			end.accept(items);
		}else {
			if (current.equals(none)){
				new ItemCreatorGUI(item -> {
					if (item != null) inv.setItem(slot, item);
					if (!addItem(p)) {
						Inventories.put(p, this, inv);
						p.openInventory(inv);
					}
				}, true).create(p);
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