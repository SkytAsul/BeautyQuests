package fr.skytasul.quests.gui.npc;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.RunnableObj;

public class SelectGUI implements CustomInventory{
	
	public static ItemStack createNPC = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.createNPC.toString());
	public static ItemStack selectNPC = ItemUtils.item(XMaterial.STICK, Lang.selectNPC.toString());
	
	private  RunnableObj run;
	
	public Inventory inv;
	
	public SelectGUI(RunnableObj run) {
		this.run = run;
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, 9, Lang.INVENTORY_SELECT.toString());
		
		inv.setItem(6, createNPC.clone());
		inv.setItem(7, selectNPC.clone());
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		case 6:
			NPCGUI tmp = (NPCGUI) Inventories.create(p, new NPCGUI());
			tmp.run = (obj) -> {
				if (obj == null){
					Inventories.put(p, openLastInv(p), inv);
				}else run.run(obj);
			};
			break;

		case 7:
			Editor.enterOrLeave(p, new SelectNPC(p, (obj) -> {
				if (obj == null){
					p.openInventory(inv);
				}else run.run(obj);
			}));
			break;
		}
		return true;
	}

}