package fr.skytasul.quests.utils.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;

public abstract class NMS{
	
	private ReflectUtils nmsReflect = ReflectUtils.fromPackage("net.minecraft.server." + getClass().getSimpleName());
	private ReflectUtils craftReflect = ReflectUtils.fromPackage("org.bukkit.craftbukkit." + getClass().getSimpleName());

	private Field unhandledTags, repairCost;
	private Method equalsCommon;

	public NMS() {
		if (!(this instanceof NullNMS)) {
			try {
				Class<?> itemMetaClass = craftReflect.fromName("inventory.CraftMetaItem");
				unhandledTags = itemMetaClass.getDeclaredField("unhandledTags");
				repairCost = itemMetaClass.getDeclaredField("repairCost");
				equalsCommon = itemMetaClass.getDeclaredMethod("equalsCommon", itemMetaClass);
				unhandledTags.setAccessible(true);
				repairCost.setAccessible(true);
				equalsCommon.setAccessible(true);
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract Object bookPacket(ByteBuf buf);

	public abstract Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt, Object paramData);
	
	public abstract double entityNameplateHeight(LivingEntity en); // can be remplaced by Entity.getHeight from 1.11
	
	public abstract Object getIChatBaseComponent(String text);
	
	public abstract Object getEnumChatFormat(int value);
	
	public List<String> getAvailableBlockProperties(Material material){
		throw new UnsupportedOperationException();
	}
	
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		((Map<?, ?>) unhandledTags.get(meta1)).clear();
		((Map<?, ?>) unhandledTags.get(meta2)).clear();
		repairCost.set(meta2, repairCost.get(meta1)); //set same repair cost
		return (boolean) equalsCommon.invoke(meta1, meta2);
	}
	
	public ReflectUtils getNMSReflect(){
		return nmsReflect;
	}
	
	public ReflectUtils getCraftReflect(){
		return craftReflect;
	}
	
	public abstract void sendPacket(Player p, Object packet);
    
    public static NMS getNMS(){
    	return nms;
    }
    
    public static boolean isValid(){
    	return versionValid;
    }
    
    public static int getMCVersion(){
    	return MCversion;
    }
    
    private static boolean versionValid = false;
	private static NMS nms;
	private static int MCversion;
	
	static {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
		MCversion = Integer.parseInt(version.split("_")[1]);
		try {
			nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + version).newInstance();
			versionValid = true;
			BeautyQuests.logger.info("Loaded valid Minecraft version " + version + ".");
		}catch (ClassNotFoundException ex) {
			BeautyQuests.logger.warning("The Minecraft version " + version + " is not supported by BeautyQuests.");
		}catch (Exception ex) {
			ex.printStackTrace();
			BeautyQuests.logger.warning("An error ocurred when loading Minecraft Server version " + version + " compatibilities.");
		}
		if (!versionValid) {
			nms = new NullNMS();
			BeautyQuests.logger.warning("Some functionnalities of the plugin have not been enabled.");
		}
	}
	
}
