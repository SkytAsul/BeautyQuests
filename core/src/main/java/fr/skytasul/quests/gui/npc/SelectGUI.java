package fr.skytasul.quests.gui.npc;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class SelectGUI implements CustomInventory{
	
	public static ItemStack createNPC = ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.createNPC.toString());
	public static ItemStack selectNPC = ItemUtils.item(XMaterial.STICK, Lang.selectNPC.toString());
	
	private Runnable cancel;
	private Consumer<BQNPC> run;
	private boolean nullable;
	
	public Inventory inv;
	
	public SelectGUI(Runnable cancel, Consumer<BQNPC> run) {
		this.cancel = cancel;
		this.run = run;
	}
	
	public SelectGUI setNullable() {
		this.nullable = true;
		return this;
	}
	
	public CustomInventory openLastInv(Player p) {
		p.openInventory(inv);
		return this;
	}
	
	@Override
	public Inventory open(Player p){
		inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_SELECT.toString());
		
		inv.setItem(1, createNPC.clone());
		inv.setItem(3, selectNPC.clone());
		if (nullable) inv.setItem(2, ItemUtils.item(XMaterial.BARRIER, "§cNone"));
		
		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	@Override
	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){

		case 1:
			new NPCGUI(run, () -> Inventories.put(p, openLastInv(p), inv)).create(p);
			break;
		
		case 2:
			Inventories.closeAndExit(p);
			run.accept(null);
			break;

		case 3:
			new SelectNPC(p, () -> openLastInv(p), run).enter();
			break;
		}
		return true;
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(cancel);
		return CloseBehavior.NOTHING;
	}

}