package fr.skytasul.quests.gui.creation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Pair;
import fr.skytasul.quests.utils.types.RunnableObj;

public class CommandGUI implements CustomInventory {
	
	public CommandGUI(RunnableObj end){
		run = end;
	}
	
	private RunnableObj run;
	private Inventory inv;
	
	private String cmd;
	private boolean console = false;
	
	
	public Inventory open(Player p) {
		inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_COMMAND.toString());
		
		inv.setItem(0, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.commandValue.toString()));
		inv.setItem(4, ItemUtils.itemSwitch(Lang.console.toString(), false));

		inv.setItem(2, ItemUtils.itemDone());
		inv.getItem(2).setType(Material.COAL);

		inv = p.openInventory(inv).getTopInventory();
		return inv;
	}

	
	public void onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot){
		case 0:
			Lang.COMMAND.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				cmd = (String) obj;
				inv.getItem(2).setType(Material.DIAMOND);
				p.openInventory(inv);
			}));
			break;
			
		case 4:
			/*if (current.getDurability() == 8){
				current.setDurability((short) 10);
				ItemUtils.name(current, "§a" + Lang.console.toString());
				console = true;
			}else {
				current.setDurability((short) 8);
				ItemUtils.name(current, "§8" + Lang.console.toString());
				console = false;
			}*/
			console = ItemUtils.toggle(current);
			break;
			
		case 2:
			if (current.getType() == Material.DIAMOND){
				run.run(new Pair<String, Boolean>(cmd, console));
			}
			break;
			
		}
	}

}
