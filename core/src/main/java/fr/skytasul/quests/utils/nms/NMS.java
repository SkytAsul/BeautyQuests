package fr.skytasul.quests.utils.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;

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
	
	public abstract Object bookPacket(ByteBuf buf);
	
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
	
	public abstract void sendPacket(Player p, Object packet);
	
	public static NMS getNMS() {
		return nms;
	}
	
	public static boolean isValid() {
		return versionValid;
	}
	
	private static boolean versionValid = false;
	private static NMS nms;
	
	static {
		try {
			nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + MinecraftVersion.VERSION_NMS).newInstance();
			versionValid = true;
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.info("Loaded valid Minecraft version " + MinecraftVersion.VERSION_NMS + ".");
		}catch (ClassNotFoundException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("The Minecraft version " + MinecraftVersion.VERSION_NMS + " is not supported by BeautyQuests.");
		}catch (Exception ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("An error ocurred when loading Minecraft Server version "
					+ MinecraftVersion.VERSION_NMS + " compatibilities.", ex);
		}
		if (!versionValid) {
			nms = new NullNMS();
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Some functionnalities of the plugin have not been enabled.");
		}
	}
	
}
