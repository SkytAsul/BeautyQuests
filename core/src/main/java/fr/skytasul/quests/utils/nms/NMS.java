package fr.skytasul.quests.utils.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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

	private Field unhandledTags;
	private Method equalsCommon;

	public NMS() {
		if (versionValid) {
			try {
				Class<?> itemMetaClass = craftReflect.fromName("inventory.CraftMetaItem");
				unhandledTags = itemMetaClass.getDeclaredField("unhandledTags");
				equalsCommon = itemMetaClass.getDeclaredMethod("equalsCommon", itemMetaClass);
			}catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}

	public abstract Object bookPacket(ByteBuf buf);

	public abstract Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt, Object paramData);
	
	public abstract double entityNameplateHeight(LivingEntity en); // can be remplaced by Entity.getHeight from 1.11
	
	public Object newPacket(String name, Object... params){
		try {
			Class<?> c = getNMSReflect().fromName(name);
			Class<?>[] array = new Class<?>[params.length];
			for (int i = 0; i < params.length; i++){
				array[i] = params[i].getClass();
			}
			return c.getConstructor(array).newInstance(params);
		}catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return params;
	}
	
	public abstract Object getIChatBaseComponent(String text);
	
	public abstract Object getEnumChatFormat(int value);
	
	public List<String> getAvailableBlockProperties(Material material){
		throw new UnsupportedOperationException();
	}
	
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		unhandledTags.setAccessible(true);
		equalsCommon.setAccessible(true);
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
	private static final List<String> validVersions = Arrays.asList("1_9_R1", "1_9_R2", "1_10_R1", "1_11_R1", "1_12_R1", "1_13_R2", "1_14_R1", "1_15_R1", "1_16_R1", "1_16_R2");
	
	static {
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
		if (validVersions.contains(version)){
			try{
				versionValid = true;
				nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + version).newInstance();
				MCversion = Integer.parseInt(version.split("_")[1]);
			}catch (Exception ex) {
				ex.printStackTrace();
				versionValid = false;
				nms = new NullNMS();
			}
		}else nms = new NullNMS();
		BeautyQuests.logger.info((versionValid) ? "Loaded valid version " + nms.getClass().getSimpleName() : "Minecraft Server version is not valid for this server. Some functionnality aren't enable. Currenttly accepted versions are: " + String.join(", ", validVersions));
	}
	
}
