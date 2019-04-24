package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BlockData;
import fr.skytasul.quests.utils.types.RunnableObj;

public class SelectBlockGUI implements CustomInventory{
	
	private ItemStack amount = item(XMaterial.REDSTONE, "§a" + Lang.Amount.toString() + " : §b§l");
	private ItemStack done = item(XMaterial.DIAMOND, Lang.done.toString());
	
	public RunnableObj run;
	
	public Inventory lastInv;
	
	BlockData block = new BlockData(XMaterial.STONE, 1);
	
	public String name() {
		return Lang.INVENTORY_SELECT.toString();
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(lastInv);
		return this;
	}
	
	public Inventory open(Player p){
		lastInv = Bukkit.createInventory(null, 9, name());
		
		lastInv.setItem(8, done.clone());
		updateItems();
		
		p.openInventory(lastInv);
		return lastInv;
	}

	public void updateItems(){
		lastInv.setItem(1, ItemUtils.nameAdd(amount.clone(), "" + block.amount));
		lastInv.setItem(3, item(block.type, "Material: " + block.type.name()));
	}
	
	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		default:
			break;
			
		case 1:
			Inventories.closeWithoutExit(p);
			Utils.sendMessage(p, "Write the amount of blocks to mine.");
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				block.amount = (int) obj;
				openLastInv(p);
				updateItems();
			}, new NumberParser(Integer.class, true, true)));
			break;
			
		case 3:
			Inventories.closeWithoutExit(p);
			Utils.sendMessage(p, "Write the name of the block to mine.");
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				XMaterial type = (XMaterial) obj; block.type = type;
					openLastInv(p);
					updateItems();
			}, new MaterialParser(false)));
			break;
			
		case 8:
			run.run(block);
			break;
			
		}
	} 

	public boolean onClose(Player p, Inventory inv){
		new BukkitRunnable() {
			
			public void run(){
				p.openInventory(inv);
			}
		}.runTaskLater(BeautyQuests.getInstance(), 1L);
		return false;
	}

}