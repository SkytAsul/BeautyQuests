package fr.skytasul.quests.gui.misc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class ConfirmGUI implements CustomInventory {

	private Runnable yes, no;
	
	private boolean close = false;
	
	public ConfirmGUI(Runnable yes, Runnable no) {
		this.yes = yes;
		this.no = no;
	}
	
	
	public Inventory open(Player p){
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_CONFIRM.toString());
		
		inv.setItem(1, ItemUtils.item(XMaterial.LIME_DYE, Lang.confirmYes.toString()));
		inv.setItem(3, ItemUtils.item(XMaterial.ROSE_RED, Lang.confirmNo.toString()));
		
		close = false;
		return p.openInventory(inv).getTopInventory();
	}

	
	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click){
		close = true;
		p.closeInventory();
		if (slot == 1) {
			yes.run();
		}else if (slot == 3) {
			no.run();
		}
	}
	
	
	public boolean onClose(Player p, Inventory inv){
		if (!close) {
			new BukkitRunnable() {
				
				public void run(){
					no.run();
				}
			}.runTaskLater(BeautyQuests.getInstance(), 1L);
		}
		return true;
	}

}
