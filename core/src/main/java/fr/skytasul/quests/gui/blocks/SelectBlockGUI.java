package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;

import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.types.BQBlock;

public class SelectBlockGUI implements CustomInventory{
	
	private ItemStack done = item(XMaterial.DIAMOND, Lang.done.toString());
	
	public BiConsumer<BQBlock, Integer> run;
	
	public Inventory inv;
	
	private XMaterial type = XMaterial.STONE;
	private String blockData = null;
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
		
		inv.setItem(1, item(XMaterial.REDSTONE, Lang.Amount.format(amount)));
		if (NMS.getMCVersion() >= 13) inv.setItem(5, item(XMaterial.COMMAND_BLOCK, Lang.blockData.toString(), Lang.NotSet.toString()));
		inv.setItem(8, done.clone());
		updateTypeItem();
		
		return inv = p.openInventory(inv).getTopInventory();
	}

	private void updateTypeItem() {
		inv.setItem(3, item(type, Lang.materialName.format(type.name())));
		if (inv.getItem(3) == null || inv.getItem(3).getType() == Material.AIR) inv.setItem(3, item(XMaterial.STONE, Lang.materialName.format(type.name())));
	}
	
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		default:
			break;
			
		case 1:
			Lang.BLOCKS_AMOUNT.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				amount = (int) obj;
				ItemUtils.lore(current, Lang.Amount.format(amount));
				openLastInv(p);
			}, new NumberParser(Integer.class, true, true)));
			break;
			
		case 3:
			Lang.BLOCK_NAME.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				XMaterial type = (XMaterial) obj;
				this.type = type;
				if (blockData != null) {
					try {
						Bukkit.createBlockData(type.parseMaterial(), blockData);
					}catch (Exception ex) {
						Lang.INVALID_BLOCK_DATA.send(p, blockData, type.name());
						blockData = null;
						ItemUtils.lore(inv.getItem(5), Lang.NotSet.toString());
					}
				}
				updateTypeItem();
				openLastInv(p);
			}, new MaterialParser(false)));
			break;
		
		case 5:
			Lang.BLOCK_DATA.send(p, String.join(", ", NMS.getNMS().getAvailableBlockProperties(type.parseMaterial())));
			new TextEditor(p, obj -> {
				String tmp = "[" + obj + "]";
				try {
					Bukkit.createBlockData(type.parseMaterial(), tmp);
					blockData = tmp;
				}catch (Exception ex) {
					ex.printStackTrace();
					Lang.INVALID_BLOCK_DATA.send(p, tmp, type.name());
					blockData = null;
				}
				ItemUtils.lore(current, blockData == null ? Lang.NotSet.toString() : blockData);
				openLastInv(p);
			}).enterOrLeave(p);
			break;

		case 8:
			Inventories.closeAndExit(p);
			run.accept(blockData == null ? new BQBlock(type) : new BQBlock(Bukkit.createBlockData(type.parseMaterial(), blockData)), amount);
			break;
			
		}
		return true;
	}

	public CloseBehavior onClose(Player p, Inventory inv){
		return CloseBehavior.REOPEN;
	}

}