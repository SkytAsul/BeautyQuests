package fr.skytasul.quests.api.utils;

import org.bukkit.Bukkit;

public final class MinecraftVersion {

	public static final String VERSION_STRING;
	public static final String VERSION_NMS;
	public static final int MAJOR;
	public static final int MINOR;

	static {
		VERSION_STRING = Bukkit.getBukkitVersion().split("-R")[0];
		VERSION_NMS = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
		String[] versions = VERSION_NMS.split("_");
		MAJOR = Integer.parseInt(versions[1]); // 1.X
		if (MAJOR >= 17) {
			// e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
			versions = VERSION_STRING.split("\\.");
			MINOR = versions.length <= 2 ? 0 : Integer.parseInt(versions[2]);
		} else
			MINOR = Integer.parseInt(versions[2].substring(1)); // 1.X.Y
	}

	private MinecraftVersion() {}

}
