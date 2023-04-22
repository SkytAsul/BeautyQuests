package fr.skytasul.quests.gui.templates;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.gui.CustomInventory;

public abstract class ChooseGUI<T> implements CustomInventory {

	private List<T> available;
	
	public ChooseGUI(List<T> available){
		this.available = available;
	}
	
	public Inventory open(Player p){
		Inventory inv = Bukkit.createInventory(null, (int) Math.ceil(available.size() * 1.0 / 9.0)*9, name());
		
		for (int i = 0; i < available.size(); i++) {
			inv.setItem(i, getItemStack(available.get(i)));
		}
		
		return p.openInventory(inv).getTopInventory();
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		finish(available.get(slot));
		return true;
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REOPEN;
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
	 * Called when the player click on an item
	 */
	public abstract void finish(T object);
	
}
