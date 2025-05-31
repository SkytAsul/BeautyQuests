package fr.skytasul.quests.utils.nms;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.ReflectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class NMS{

	protected final ReflectUtils nmsReflect = ReflectUtils.fromPackage("net.minecraft.server." + getClass().getSimpleName());
	protected final ReflectUtils craftReflect =
			ReflectUtils.fromPackage("org.bukkit.craftbukkit." + getClass().getSimpleName());

	protected Field unhandledTags;
	protected Method equalsCommon;

	public NMS() {
		if (!(this instanceof NullNMS)) {
			try {
				Class<?> itemMetaClass = craftReflect.fromName("inventory.CraftMetaItem");
				unhandledTags = itemMetaClass.getDeclaredField("unhandledTags");
				equalsCommon = itemMetaClass.getDeclaredMethod("equalsCommon", itemMetaClass);
				unhandledTags.setAccessible(true);
				equalsCommon.setAccessible(true);
			}catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public abstract double entityNameplateHeight(Entity en); // can be remplaced by Entity.getHeight from 1.11

	public List<String> getAvailableBlockProperties(Material material){
		throw new UnsupportedOperationException();
	}

	public List<String> getAvailableBlockTags() {
		throw new UnsupportedOperationException();
	}

	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		((Map<?, ?>) unhandledTags.get(meta1)).clear();
		((Map<?, ?>) unhandledTags.get(meta2)).clear();
		return (boolean) equalsCommon.invoke(meta1, meta2);
	}

	public ReflectUtils getNMSReflect(){
		return nmsReflect;
	}

	public ReflectUtils getCraftReflect(){
		return craftReflect;
	}

	public abstract void openBookInHand(Player p);

	public static NMS getNMS() {
		return nms;
	}

	public static boolean isValid() {
		return versionValid;
	}

	private static boolean versionValid = false;
	private static NMS nms;

	static {
		String versionNms;

		if (BeautyQuests.getInstance().isUnitTesting()) {
			versionNms = null;
		} else {
			try {
				versionNms = getNmsVersion().substring(1);
			} catch (Exception ex) {
				BeautyQuests.getInstance().getLoggerExpanded().severe("Cannot get server internals version", ex);
				versionNms = null;
			}
		}

		if (versionNms != null)
			nms = loadNms(versionNms);

		if (nms != null) {
			versionValid = true;
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Loaded valid Minecraft version {0}.", versionNms);
		} else {
			nms = new NullNMS();
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Some functionnalities of the plugin have not been enabled.");
		}
	}

	private static String getNmsVersion() throws ReflectiveOperationException {
		if (BeautyQuests.getInstance().isRunningPaper() && MinecraftVersion.isHigherThan(20, 5)) {
			Class<?> paperMapping = Class.forName("io.papermc.paper.util.MappingEnvironment");
			return (String) paperMapping.getDeclaredField("LEGACY_CB_VERSION").get(null);
		}

		// Spigot / legacy Paper
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}

	private static NMS loadNms(String version) {
		try {
			return (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + version).getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException __) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("The Minecraft version {0} is not supported by BeautyQuests.", version);
		} catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.severe("An error ocurred when loading Minecraft Server version {0} compatibilities.", ex, version);
		}
		return null;
	}

}
