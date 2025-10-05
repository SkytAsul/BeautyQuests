package fr.skytasul.quests.utils.nms;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.ReflectUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class NMS{

	protected final ReflectUtils nmsReflect;
	protected final ReflectUtils craftReflect;

	protected Field unhandledTags;
	protected Method equalsCommon;

	public NMS() {
		try {
			String remappedSuffix = getMappingsVersion() == null ? "" : "." + getMappingsVersion();
			nmsReflect = ReflectUtils.fromPackage("net.minecraft.server" + remappedSuffix);
			craftReflect = ReflectUtils.fromPackage("org.bukkit.craftbukkit" + remappedSuffix);
			if (!(this instanceof NullNMS)) {
				Class<?> itemMetaClass = craftReflect.fromName("inventory.CraftMetaItem");
				unhandledTags = itemMetaClass.getDeclaredField("unhandledTags");
				equalsCommon = itemMetaClass.getDeclaredMethod("equalsCommon", itemMetaClass);
				unhandledTags.setAccessible(true);
				equalsCommon.setAccessible(true);
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getAvailableBlockProperties(Material material){
		throw new UnsupportedOperationException();
	}

	public List<Key> getAvailableBlockTags() {
		throw new UnsupportedOperationException();
	}

	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		((Map<?, ?>) unhandledTags.get(meta1)).clear();
		((Map<?, ?>) unhandledTags.get(meta2)).clear();
		return (boolean) equalsCommon.invoke(meta1, meta2);
	}

	public static NMS getNMS() {
		return nms;
	}

	private static NMS nms;

	static {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Server version: {0}", Bukkit.getVersion());
		if (BeautyQuests.getInstance().isUnitTesting()) {
			nms = new NullNMS();
		} else if (BeautyQuests.getInstance().isRunningPaper() && MinecraftVersion.isHigherThan(20, 5)) {
			loadPaperNms();
		} else {
			try {
				String versionNms = getMappingsVersion().substring(1);
				loadVersionedNms(versionNms);
			} catch (Exception ex) {
				BeautyQuests.getInstance().getLoggerExpanded().severe("Cannot get server internals version", ex);
			}
		}

		if (nms == null) {
			nms = new NullNMS();
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Some functionnalities of the plugin have not been enabled.");
		}
	}

	private static String getMappingsVersion() throws ReflectiveOperationException {
		if (BeautyQuests.getInstance().isRunningPaper() && MinecraftVersion.isHigherThan(20, 5)) {
			// Plugin is not remapped
			return null;
		}

		// Spigot / legacy Paper
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}

	private static void loadVersionedNms(String version) {
		try {
			nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + version).getDeclaredConstructor().newInstance();
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Loaded valid remapped internals access for {0}.", version);
		} catch (ClassNotFoundException __) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("The Minecraft version {0} is not supported by BeautyQuests.", version);
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("An error ocurred when loading internals compatibility for version {0}.", ex, version);
		}
	}

	private static void loadPaperNms() {
		try {
			nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.PaperNMS").getDeclaredConstructor().newInstance();
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Loaded valid Paper internals access.");
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("An error ocurred when loading internals compatibilities for Paper.", ex);
		}
	}

}
