package fr.skytasul.quests.gui.permissions;

import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.checkers.WorldParser;
import fr.skytasul.quests.api.gui.Gui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.utils.types.Permission;

public class PermissionGUI extends Gui {

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

	@Override
	protected Inventory instanciate(@NotNull Player player) {
		return Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_PERMISSION.toString());
	}

	@Override
	protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
		inventory.setItem(0, ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.perm.toString(),
				perm == null ? Lang.NotSet.toString() : "§b" + perm));
		inventory.setItem(1, ItemUtils.item(XMaterial.FILLED_MAP, Lang.world.toString(),
				world == null ? Lang.worldGlobal.toString() : "§b" + world));
		inventory.setItem(2, ItemUtils.itemSwitch(Lang.permRemove.toString(), take, Lang.permRemoveLore.toString()));

		ItemStack done = ItemUtils.itemDone.toMutableStack();
		if (perm == null) done.setType(Material.COAL);
		inventory.setItem(4, done);
	}

	@Override
	public void onClick(GuiClickEvent event) {
		switch (slot) {
		case 0:
			Lang.CHOOSE_PERM_REWARD.send(p);
			new TextEditor<String>(p, () -> reopen(p), x -> {
				getInventory().getItem(4).setType(Material.DIAMOND);
				updatePerm(p, x);
			}, () -> {
				getInventory().getItem(4).setType(Material.COAL);
				updatePerm(p, null);
			}).useStrippedMessage().start();
			break;
		case 1:
			Lang.CHOOSE_PERM_WORLD.send(p);
			new TextEditor<>(p, () -> reopen(p), worldS -> {
				updateWorld(p, worldS.getName());
			}, () -> {
				updateWorld(p, null);
			}, new WorldParser()).start();
			break;
		case 2:
			take = ItemUtils.toggleSwitch(current);
			break;
		case 4:
			if (current.getType() == Material.COAL) break;
			end.accept(new Permission(perm, take, world));
			break;
		}
		return true;
	}
	
	private void updatePerm(Player p, String perm) {
		this.perm = perm;
		ItemUtils.lore(getInventory().getItem(0), perm == null ? Lang.NotSet.toString() : "§b" + perm);
		reopen(p);
	}

	private void updateWorld(Player p, String name) {
		world = name;
		ItemUtils.lore(getInventory().getItem(1), world == null ? Lang.worldGlobal.toString() : "§b" + world);
		reopen(p);
	}

	@Override
	public CloseBehavior onClose(Player p) {
		return StandardCloseBehavior.REOPEN;
	}
	
}
