	package fr.skytasul.quests.gui.misc;

import java.util.List;

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
 * An inventory who has 8 slots to store items. Each item is linked in a list to an instance of type T.
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the list
 */
public abstract class ListGUI<T> implements CustomInventory {
	
	private ItemStack none = ItemUtils.item(XMaterial.RED_STAINED_GLASS_PANE, "§c");

	private List<T> objects;
	protected Inventory inv;
	protected Player p;
	
	public ListGUI(List<T> list){
		this.objects = list;
	}
	
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, name());
		this.p = p;
		
		inv.setItem(8, ItemUtils.itemDone());
		for (int i = 0; i < 8; i++){
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
		if (slot == 8){
			finish();
		}else {
			if (current.equals(none)){
				click(null);
			}else if (click == ClickType.MIDDLE){
				remove(slot);
				//inv.setItem(slot, none);
			}else {
				T obj = objects.get(slot);
				//objects.remove(slot);
				remove(slot);
				click(obj);
			}
		}
		return true;
	}
	
	public void finishItem(T object){
		Inventories.closeWithoutExit(p);
		inv = p.openInventory(inv).getTopInventory();
		Inventories.put(p, this, inv);
		objects.add(object);
		inv.setItem(objects.size() - 1, getItemStack(object));
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
	 * @param existing clicked object
	 */
	public abstract void click(T existing);
	
	/**
	 * Called when the player hit the finish button
	 */
	public abstract void finish();
	
}
