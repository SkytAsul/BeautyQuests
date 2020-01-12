package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;

import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class SelectBlockGUI implements CustomInventory{
	
	private ItemStack done = item(XMaterial.DIAMOND, Lang.done.toString());
	
	public BiConsumer<XMaterial, Integer> run;
	
	public Inventory inv;
	
	private XMaterial type = XMaterial.STONE;
	private int amount = 1;
	
	public String name() {
		return Lang.INVENTORY_BLOCK.toString();
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, name());
		
		inv.setItem(8, done.clone());
		updateItems();
		
		return inv = p.openInventory(inv).getTopInventory();
	}

	public void updateItems(){
		inv.setItem(1, item(XMaterial.REDSTONE, Lang.Amount.format(amount)));
		inv.setItem(3, item(type, Lang.materialName.format(type.name())));
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		default:
			break;
			
		case 1:
			Inventories.closeWithoutExit(p);
			Lang.BLOCKS_AMOUNT.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				amount = (int) obj;
				openLastInv(p);
				updateItems();
			}, new NumberParser(Integer.class, true, true)));
			break;
			
		case 3:
			Inventories.closeWithoutExit(p);
			Lang.BLOCKS_NAME.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				XMaterial type = (XMaterial) obj;
				this.type = type;
				openLastInv(p);
				updateItems();
			}, new MaterialParser(false)));
			break;

		case 8:
			Inventories.closeAndExit(p);
			run.accept(type, amount);
			break;
			
		}
		return true;
	}

	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REOPEN;
	}

}