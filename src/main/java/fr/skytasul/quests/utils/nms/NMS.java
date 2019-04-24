package fr.skytasul.quests.utils.nms;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;

public abstract class NMS{
	
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
	
	private ReflectUtils nmsReflect = ReflectUtils.fromPackage("net.minecraft.server." + getClass().getSimpleName());
	private ReflectUtils craftReflect = ReflectUtils.fromPackage("org.bukkit.craftbukkit." + getClass().getSimpleName());
	
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
	private static final List<String> validVersions = Arrays.asList("1_9_R1", "1_9_R2", "1_10_R1", "1_11_R1", "1_12_R1", "1_13_R2", "1_14_R1");
	
	public static void intializeNMS(){
		String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
		if (validVersions.contains(version)){
			try{
				nms = (NMS) Class.forName("fr.skytasul.quests.utils.nms.v" + version).newInstance();
				versionValid = true;
				MCversion = Integer.parseInt(version.split("_")[1]);
			}catch (Throwable ex){
				versionValid = false;
				nms = new NullNMS();
				ex.printStackTrace();
			}
		}else nms = new NullNMS();
		BeautyQuests.logger.info((versionValid) ? "Loaded valid version " + nms.getClass().getSimpleName() : "Minecraft Server version is not valid for this server. Some functionnality aren't enable. Current accepted versions : 1.11, 1.12");
	}
	
}
