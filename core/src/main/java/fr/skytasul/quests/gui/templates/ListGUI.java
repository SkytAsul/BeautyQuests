	package fr.skytasul.quests.gui.templates;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.XMaterial;

/**
 * An inventory which has up to 54 slots to store items. Each item is linked in a list to an instance of type T.
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the list
 */
public abstract class ListGUI<T> implements CustomInventory {
	
	private ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c");

	protected List<T> objects;
	protected int size;
	
	protected Inventory inv;
	protected Player p;
	
	public ListGUI(List<T> list, int size){
		this.objects = list;
		Validate.isTrue(size % 9 == 0, "Size must be a multiple of 9");
		this.size = size;
	}
	
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, size, name());
		this.p = p;
		
		inv.setItem(size - 1, ItemUtils.itemDone);
		for (int i = 0; i < size - 1; i++) {
			if (objects.size() <= i){
				inv.setItem(i, none);
			}else {
				inv.setItem(i, getItemStack(objects.get(i)));
			}
		}

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}
	
	public void remove(int slot){
		objects.remove(slot);
		for (int i = slot; i <= objects.size(); i++){
			inv.setItem(i, i == objects.size() ? none : inv.getItem(i+1));
		}
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		if (slot == size - 1){
			finish();
		}else {
			if (current.equals(none)){
				click(null, null);
			}else if (click == ClickType.MIDDLE){
				remove(slot);
			}else {
				T obj = objects.get(slot);
				remove(slot);
				click(obj, current);
			}
		}
		return true;
	}
	
	/**
	 * Call this when an object is ready to be inserted in the list
	 * @param object Object to put
	 * @return ItemStack created with {@link #getItemStack(Object)}
	 */
	public ItemStack finishItem(T object){
		Inventories.closeWithoutExit(p);
		inv = p.openInventory(inv).getTopInventory();
		Inventories.put(p, this, inv);
		objects.add(object);
		int slot = objects.size() - 1;
		inv.setItem(slot, getItemStack(object));
		return inv.getItem(slot);
	}
	
	/**
	 * @return Inventory's name
	 */
	public abstract String name();
	
	/**
	 * @param object existing object to represent
	 * @return ItemStack who represents the object
	 */
	public abstract ItemStack getItemStack(T object);
	
	/**
	 * Called when an object is clicked
	 * @param existing clicked object (may be null if there was no previous object)
	 * @param item clicked item
	 */
	public abstract void click(T existing, ItemStack item);
	
	/**
	 * Called when the player hit the finish button
	 */
	public abstract void finish();
	
}
