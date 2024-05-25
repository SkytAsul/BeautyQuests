package fr.skytasul.quests.api.utils;

import org.bukkit.Bukkit;

public final class MinecraftVersion {

	public static final String VERSION_STRING;
	public static final int MAJOR;
	public static final int MINOR;

	static {
		// e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
		VERSION_STRING = Bukkit.getBukkitVersion().split("-R")[0];
		String[] versions = VERSION_STRING.split("\\.");
		MAJOR = Integer.parseInt(versions[1]);
		MINOR = versions.length <= 2 ? 0 : Integer.parseInt(versions[2]);
	}

	private MinecraftVersion() {}

	public static boolean isHigherThan(int major, int minor) {
		if (MAJOR > major)
			return true;

		if (MAJOR == major && MINOR >= minor)
			return true;

		return false;
	}

}
