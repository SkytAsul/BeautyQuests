package fr.skytasul.quests.requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.ListGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.objects.QuestObjectLoreBuilder;
import fr.skytasul.quests.api.requirements.AbstractRequirement;

public class PermissionsRequirement extends AbstractRequirement {

	private List<Permission> permissions;
	
	public PermissionsRequirement() {
		this(null, null, new ArrayList<>());
	}
	
	public PermissionsRequirement(String customDescription, String customReason, List<Permission> permissions) {
		super(customDescription, customReason);
		this.permissions = permissions;
	}

	@Override
	public boolean test(Player p) {
		for (Permission perm : permissions) {
			if (!perm.match(p)) return false;
		}
		return true;
	}

	@Override
	protected void addLore(QuestObjectLoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription(Lang.AmountPermissions.format(permissions.size()));
	}
	
	@Override
	protected void sendCustomReasonHelpMessage(Player p) {
		Lang.CHOOSE_PERM_REQUIRED_MESSAGE.send(p);
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
				Lang.CHOOSE_PERM_REQUIRED.send(player);
				new TextEditor<String>(player, this::reopen, obj -> {
					callback.apply(Permission.fromString(obj));
				}).useStrippedMessage().start();
			}
			
			@Override
			public void finish(List<Permission> objects) {
				permissions = objects;
				Lang.CHOOSE_PERM_REQUIRED_MESSAGE.send(player);
				new TextEditor<String>(player, event::reopenGUI, obj -> {
					setCustomReason(obj);
					event.reopenGUI();
				}).passNullIntoEndConsumer().start();
			}
			
		}.open(event.getPlayer());
	}
	
	@Override
	public AbstractRequirement clone() {
		return new PermissionsRequirement(getCustomDescription(), getCustomReason(), new ArrayList<>(permissions));
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("permissions", permissions.stream().map(Permission::toString).collect(Collectors.toList()));
	}
	
	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		permissions = section.getStringList("permissions").stream().map(Permission::fromString).collect(Collectors.toList());
		if (section.contains("message")) // migration from 0.20.1 and before, TODO delete
			setCustomReason(section.getString("message"));
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
