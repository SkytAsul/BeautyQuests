package fr.skytasul.quests.utils.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.ReflectUtils;

public abstract class NMS{
	
	private ReflectUtils nmsReflect = ReflectUtils.fromPackage("net.minecraft.server." + getClass().getSimpleName());
	private ReflectUtils craftReflect = ReflectUtils.fromPackage("org.bukkit.craftbukkit." + getClass().getSimpleName());
	
	private Field unhandledTags;
	private Method equalsCommon;
	
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
	
	public static int getMCVersion() {
		return versionMajor;
	}
	
	public static int getNMSMinorVersion() {
		return versionMinorNMS;
	}
	
	public static String getVersionString() {
		return versionString;
	}
	
	private static boolean versionValid = false;
	private static NMS nms;
	private static int versionMajor;
	private static int versionMinorNMS;
	private static String versionString;
	
	static {
		versionString = Bukkit.getBukkitVersion().split("-R")[0];
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].substring(1);
		String[] versions = version.split("_");
		versionMajor = Integer.parseInt(versions[1]); // 1.X
		if (versionMajor >= 17) {
			// e.g. Bukkit.getBukkitVersion() -> 1.17.1-R0.1-SNAPSHOT
			versions = versionString.split("\\.");
			versionMinorNMS = versions.length <= 2 ? 0 : Integer.parseInt(versions[2]);
		}else versionMinorNMS = Integer.parseInt(versions[2].substring(1)); // 1.X.Y
		
		try {
			nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + version).newInstance();
			versionValid = true;
			BeautyQuests.logger.info("Loaded valid Minecraft version " + version + ".");
		}catch (ClassNotFoundException ex) {
			BeautyQuests.logger.warning("The Minecraft version " + version + " is not supported by BeautyQuests.");
		}catch (Exception ex) {
			BeautyQuests.logger.warning("An error ocurred when loading Minecraft Server version " + version + " compatibilities.", ex);
		}
		if (!versionValid) {
			nms = new NullNMS();
			BeautyQuests.logger.warning("Some functionnalities of the plugin have not been enabled.");
		}
	}
	
}
