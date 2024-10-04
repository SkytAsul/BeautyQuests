package fr.skytasul.quests.integrations.vault.permission;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PermissionListGUI extends ListGUI<Permission> {

	private Consumer<List<Permission>> end;

	public PermissionListGUI(List<Permission> list, Consumer<List<Permission>> end) {
		super(Lang.INVENTORY_PERMISSION_LIST.toString(), DyeColor.PURPLE, list);
		this.end = end;
	}

	@Override
	public ItemStack getObjectItemStack(Permission object) {
		return ItemUtils.item(XMaterial.PAPER, "§e" + object.permission, createLoreBuilder(object)
				.addDescription(Lang.permRemoved.format(object))
				.addDescription(Lang.permWorld.format(object)).toLoreArray());
	}

	@Override
	public void createObject(Function<Permission, ItemStack> callback) {
		new PermissionGUI(perm -> {
			callback.apply(perm);
		}, null).open(player);
	}
	
	@Override
	public void clickObject(Permission object, ItemStack item, ClickType clickType) {
		new PermissionGUI(perm -> {
			updateObject(object, perm);
			reopen();
		}, object).open(player);
	}

	@Override
	public void finish(List<Permission> objects) {
		end.accept(objects);
	}
	
}