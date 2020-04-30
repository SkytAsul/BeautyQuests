package fr.skytasul.quests.gui.permissions;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Permission;

public class PermissionListGUI extends ListGUI<Permission> {

	private Runnable end;

	public PermissionListGUI(List<Permission> list, Runnable end) {
		super(list, 9);
		this.end = end;
	}

	public String name() {
		return Lang.INVENTORY_PERMISSION_LIST.toString();
	}

	public ItemStack getItemStack(Permission object) {
		return ItemUtils.item(XMaterial.PAPER, "§e" + object.permission, Lang.permRemoved.format(object.take ? Lang.Yes : Lang.No), Lang.permWorld.format(object.world == null ? Lang.worldGlobal.toString() : object.world));
	}

	public void click(Permission existing) {
		new PermissionGUI((perm) -> {
			super.finishItem(perm);
		}, existing).create(p);
	}

	public void finish() {
		end.run();
	}
}