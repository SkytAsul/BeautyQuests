package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import fr.skytasul.quests.utils.compatibility.Vault;

public class Permission implements Cloneable {

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
