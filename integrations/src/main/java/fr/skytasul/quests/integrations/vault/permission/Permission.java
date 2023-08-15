package fr.skytasul.quests.integrations.vault.permission;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.integrations.vault.Vault;

public class Permission implements Cloneable, HasPlaceholders {

	public final String permission, world;
	public final boolean take;

	public Permission(String permission, boolean take, String world) {
		this.permission = permission;
		this.take = take;
		this.world = world;
	}

	public void give(Player p) {
		Vault.changePermission(p, permission, take, world);
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		return PlaceholderRegistry.of("permission", permission, "permission_removed", MessageUtils.getYesNo(take),
				"permission_world", world == null ? Lang.worldGlobal.toString() : world);
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		map.put("perm", permission);
		if (take) map.put("take", take);
		if (world != null) map.put("world", world);

		return map;
	}

	public static Permission deserialize(Map<String, Object> map) {
		return new Permission((String) map.get("perm"), map.containsKey("take") ? (boolean) map.get("take") : false, (String) map.get("world"));
	}

}
