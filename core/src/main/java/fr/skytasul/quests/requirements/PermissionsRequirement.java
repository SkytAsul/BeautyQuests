package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class PermissionsRequirement extends AbstractRequirement {

	public List<Permission> permissions;
	public String message;
	
	public PermissionsRequirement() {
		this(new ArrayList<>(), null);
	}
	
	public PermissionsRequirement(List<Permission> permissions, String message) {
		this.permissions = permissions;
		this.message = message;
	}

	@Override
	public boolean test(Player p) {
		for (Permission perm : permissions) {
			if (!perm.match(p)) return false;
		}
		return true;
	}
	
	@Override
	public void sendReason(Player p){
		if (message != null) Utils.IsendMessage(p, message, true);
	}

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + Lang.AmountPermissions.format(permissions.size()), "§8> Message: §7" + (message == null ? Lang.NotSet.toString() : message), "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		new ListGUI<Permission>(Lang.INVENTORY_PERMISSION_LIST.toString(), DyeColor.PURPLE, permissions) {
			
			@Override
			public ItemStack getObjectItemStack(Permission object) {
				return ItemUtils.item(XMaterial.PAPER, object.toString(), "", Lang.RemoveMid.toString());
			}
			
			@Override
			public void createObject(Function<Permission, ItemStack> callback) {
				Lang.CHOOSE_PERM_REQUIRED.send(p);
				new TextEditor<String>(p, () -> p.openInventory(inv), obj -> {
					callback.apply(Permission.fromString(obj));
				}).useStrippedMessage().enter();
			}
			
			@Override
			public void finish(List<Permission> objects) {
				permissions = objects;
				Lang.CHOOSE_PERM_REQUIRED_MESSAGE.send(p);
				new TextEditor<String>(p, event::reopenGUI, obj -> {
					message = obj;
					event.reopenGUI();
				}).passNullIntoEndConsumer().enter();
			}
			
		}.create(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new PermissionsRequirement(new ArrayList<>(permissions), message);
	}
	
	@Override
	public void save(ConfigurationSection section) {
		section.set("permissions", permissions.stream().map(Permission::toString).collect(Collectors.toList()));
		if (message != null) section.set("message", message);
	}
	
	@Override
	public void load(ConfigurationSection section) {
		permissions = section.getStringList("permissions").stream().map(Permission::fromString).collect(Collectors.toList());
		if (section.contains("message")) message = section.getString("message");
	}

	public static class Permission {
		private final String permission;
		private final boolean value;

		public Permission(String permission, boolean value) {
			this.permission = permission;
			this.value = value;
		}

		public boolean match(Player p) {
			boolean has = p.hasPermission(permission);
			return has == value;
		}

		@Override
		public String toString() {
			return (value ? "" : "-") + permission;
		}

		public static Permission fromString(String string) {
			boolean neg = string.startsWith("-");
			return new Permission(string.substring(neg ? 1 : 0), !neg);
		}
	}
	
}
