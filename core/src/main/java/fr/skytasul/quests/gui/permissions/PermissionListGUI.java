package fr.skytasul.quests.gui.permissions;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.Permission;

public class PermissionListGUI extends ListGUI<Permission> {

	private Consumer<List<Permission>> end;

	public PermissionListGUI(List<Permission> list, Consumer<List<Permission>> end) {
		super(Lang.INVENTORY_PERMISSION_LIST.toString(), DyeColor.PURPLE, list);
		this.end = end;
	}

	public ItemStack getObjectItemStack(Permission object) {
		return ItemUtils.item(XMaterial.PAPER, "§e" + object.permission, Lang.permRemoved.format(object.take ? Lang.Yes : Lang.No), Lang.permWorld.format(object.world == null ? Lang.worldGlobal.toString() : object.world));
	}

	@Override
	public void createObject(Function<Permission, ItemStack> callback) {
		new PermissionGUI(perm -> {
			callback.apply(perm);
		}, null).create(p);
	}
	
	@Override
	public void clickObject(Permission object, ItemStack item, ClickType clickType) {
		new PermissionGUI(perm -> {
			updateObject(object, perm);
			reopen();
		}, object).create(p);
	}

	@Override
	public void finish(List<Permission> objects) {
		end.accept(objects);
	}
	
}