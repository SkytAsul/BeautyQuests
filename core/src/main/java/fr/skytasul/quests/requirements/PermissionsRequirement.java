package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
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
		super("permissionRequired");
		this.permissions = permissions;
		this.message = message;
	}

	public boolean test(Player p) {
		for (Permission perm : permissions) {
			if (!perm.match(p)) return false;
		}
		return true;
	}
	
	public void sendReason(Player p){
		if (message != null) Utils.IsendMessage(p, message, true);
	}

	@Override
	public String[] getLore() {
		return new String[] { "§8> §7" + permissions.size() + " permission(s)", "§8> Message: §7" + (message == null ? Lang.NotSet.toString() : message), "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		new ListGUI<Permission>(permissions, 9) {
			
			@Override
			public String name() {
				return Lang.INVENTORY_PERMISSION_LIST.toString();
			}
			
			@Override
			public ItemStack getItemStack(Permission object) {
				return ItemUtils.item(XMaterial.PAPER, object.toString(), "", Lang.Remove.toString());
			}
			
			@Override
			public void click(Permission existing, ItemStack item) {
				if (existing == null) {
					Lang.CHOOSE_PERM_REQUIRED.send(p);
					new TextEditor(p, obj -> {
						finishItem(Permission.fromString((String) obj));
					}).enterOrLeave(p);
				}
			}
			
			@Override
			public void finish() {
				Lang.CHOOSE_PERM_REQUIRED_MESSAGE.send(p);
				new TextEditor(p, obj -> {
					message = (String) obj;
					ItemUtils.lore(clicked, getLore());
					gui.reopen(p);
				}, null, () -> {
					message = null;
					ItemUtils.lore(clicked, getLore());
					gui.reopen(p);
				}).enterOrLeave(p);
			}
			
		}.create(p);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new PermissionsRequirement(new ArrayList<>(permissions), message);
	}
	
	protected void save(Map<String, Object> datas) {
		datas.put("permissions", permissions.stream().map(Permission::toString).collect(Collectors.toList()));
		if (message != null) datas.put("message", message);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		permissions = ((List<String>) savedDatas.get("permissions")).stream().map(Permission::fromString).collect(Collectors.toList());
		if (savedDatas.containsKey("message")) message = (String) savedDatas.get("message");
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

		public String toString() {
			return (value ? "" : "-") + permission;
		}

		public static Permission fromString(String string) {
			boolean neg = string.startsWith("-");
			return new Permission(string.substring(neg ? 1 : 0), !neg);
		}
	}
	
}
