package fr.skytasul.quests.integrations.vault.permission;

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
import fr.skytasul.quests.api.editors.parsers.WorldParser;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.StandardCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;

public class PermissionGUI extends AbstractGui {

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
		switch (event.getSlot()) {
		case 0:
				Lang.CHOOSE_PERM_REWARD.send(event.getPlayer());
				new TextEditor<String>(event.getPlayer(), event::reopen, x -> {
				getInventory().getItem(4).setType(Material.DIAMOND);
					updatePerm(event.getPlayer(), x);
			}, () -> {
				getInventory().getItem(4).setType(Material.COAL);
					updatePerm(event.getPlayer(), null);
			}).useStrippedMessage().start();
			break;
		case 1:
			Lang.CHOOSE_PERM_WORLD.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopen, worldS -> {
				updateWorld(event.getPlayer(), worldS.getName());
			}, () -> {
				updateWorld(event.getPlayer(), null);
			}, new WorldParser()).start();
			break;
		case 2:
			take = ItemUtils.toggleSwitch(event.getClicked());
			break;
		case 4:
			if (event.getClicked().getType() == Material.COAL)
				break;
			end.accept(new Permission(perm, take, world));
			break;
		}
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
