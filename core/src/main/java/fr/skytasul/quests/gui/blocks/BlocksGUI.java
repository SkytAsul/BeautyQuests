package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class BlocksGUI implements CustomInventory {

	private ItemStack none = item(XMaterial.RED_STAINED_GLASS_PANE, "§c", Lang.addBlock.toString());
	private ItemStack done = item(XMaterial.DIAMOND, Lang.done.toString());
	
	public Map<Integer, Entry<XMaterial, Integer>> blocks = new HashMap<>();
	
	public Inventory inv;
	public Consumer<Map<Integer, Entry<XMaterial, Integer>>> run;
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_BLOCKSLIST.toString());
		
		inv.setItem(8, done);
		for (int i = 0; i < 8; i++) inv.setItem(i, none.clone());
		
		return inv = p.openInventory(inv).getTopInventory();
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack is, int slot, ClickType click) {
		if (slot == 8){
			Inventories.closeAndExit(p);
			run.accept(blocks);
			return true;
		}
		if (click.isRightClick()){
			blocks.remove(slot);
			inv.setItem(slot, none);
			return true;
		}
		SelectBlockGUI sm = Inventories.create(p, new SelectBlockGUI());
		sm.run = (type, amount) -> {
			Inventories.put(p, openLastInv(p), inv);
			inv.setItem(slot, createItem(type, amount));
			blocks.put(slot, new AbstractMap.SimpleEntry<>(type, amount));
		};
		return true;
	}
	
	public void setBlocksFromMap(Inventory inv, Map<Integer, Entry<XMaterial, Integer>> map) {
		for (Entry<Integer, Entry<XMaterial, Integer>> entry : map.entrySet()) {
			int id = entry.getKey();
			Entry<XMaterial, Integer> blockEntry = entry.getValue();
			blocks.put(id, blockEntry);
			inv.setItem(id, createItem(blockEntry.getKey(), blockEntry.getValue()));
		}
	}

	public static ItemStack createItem(XMaterial type, int amount) {
		if (type == null) return null;
		ItemStack is = item(type, Lang.materialName.format(type.name()), Lang.Amount.format(amount));
		return is;
	}

}
