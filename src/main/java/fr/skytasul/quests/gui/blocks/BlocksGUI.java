package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BlockData;
import fr.skytasul.quests.utils.types.RunnableObj;

public class BlocksGUI implements CustomInventory {

	private ItemStack none = item(XMaterial.RED_STAINED_GLASS_PANE, "§c", "§aClick to add a block");
	private ItemStack done = item(XMaterial.DIAMOND, Lang.done.toString());
	
	public Map<Integer, BlockData> blocks = new HashMap<>();
	
	public Inventory inv;
	public RunnableObj run;
	
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
			run.run(new ArrayList<>(blocks.values()));
			return true;
		}
		if (click.isRightClick()){
			blocks.remove(slot);
			inv.setItem(slot, none);
			return true;
		}
		SelectBlockGUI sm = Inventories.create(p, new SelectBlockGUI());
		sm.run = (obj) -> {
			Inventories.put(p, openLastInv(p), inv);
			inv.setItem(slot, fromBlock((BlockData) obj));
			blocks.put(slot, (BlockData) obj);
		};
		return true;
	}
	
	public void setBlocksFromList(Inventory inv, List<BlockData> m){
		for (int i = 0; i < 8; i++){
			if (m.size() == i) break;
			blocks.put(i, m.get(i));
			inv.setItem(i, fromBlock(m.get(i)));
		}
	}

	static ItemStack fromBlock(BlockData block){
		if (block == null) return null;
		ItemStack is = item(block.type, Lang.materialName.format(block.type.name()), Lang.Amount.format(block.amount));
		return is;
	}

}
