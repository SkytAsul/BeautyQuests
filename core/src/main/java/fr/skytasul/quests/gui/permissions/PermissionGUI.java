package fr.skytasul.quests.gui.permissions;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.WorldParser;
import fr.skytasul.quests.gui.CustomInventory;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Permission;

public class PermissionGUI implements CustomInventory {

	private String perm, world = null;
	private boolean take = false;
	private Consumer<Permission> end;

	public PermissionGUI(Consumer<Permission> end, Permission existingPerm) {
		this.end = end;
		if (existingPerm == null) return;
		perm = existingPerm.permission;
		world = existingPerm.world;
		take = existingPerm.take;
	}

	public Inventory open(Player p) {
		Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_PERMISSION.toString());
		
		inv.setItem(0, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.perm.toString(), perm == null ? Lang.NotSet.toString() : "§b" + perm));
		inv.setItem(1, ItemUtils.item(XMaterial.FILLED_MAP, Lang.world.toString(), world == null ? Lang.worldGlobal.toString() : "§b" + world));
		inv.setItem(2, ItemUtils.itemSwitch(Lang.permRemove.toString(), take, Lang.permRemoveLore.toString()));

		ItemStack done = ItemUtils.itemDone.toMutableStack();
		if (perm == null) done.setType(Material.COAL);
		inv.setItem(4, done);
		
		return p.openInventory(inv).getTopInventory();
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case 0:
			Lang.CHOOSE_PERM_REWARD.send(p);
			new TextEditor<String>(p, () -> p.openInventory(inv), x -> {
				perm = x;
				inv.getItem(4).setType(Material.DIAMOND);
				p.openInventory(inv);
			}, () -> {
				perm = null;
				inv.getItem(4).setType(Material.COAL);
				p.openInventory(inv);
			}).useStrippedMessage().enter();
			break;
		case 1:
			Lang.CHOOSE_PERM_WORLD.send(p);
			new TextEditor<>(p, () -> p.openInventory(inv), worldS -> {
				updateWorld(p, worldS.getName(), inv);
			}, () -> {
				updateWorld(p, null, inv);
			}, new WorldParser()).enter();
			break;
		case 2:
			take = ItemUtils.toggle(current);
			break;
		case 4:
			if (current.getType() == Material.COAL) break;
			end.accept(new Permission(perm, take, world));
			break;
		}
		return true;
	}

	private void updateWorld(Player p, String name, Inventory inv){
		world = name;
		ItemUtils.lore(inv.getItem(1), world == null ? Lang.worldGlobal.toString() : "§b" + world);
		p.openInventory(inv);
	}

	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		return CloseBehavior.REOPEN;
	}
	
}
