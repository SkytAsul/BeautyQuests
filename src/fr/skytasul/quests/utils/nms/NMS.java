package fr.skytasul.quests.utils.nms;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;

public interface NMS{
	
	public Object bookPacket(ByteBuf buf);

	public Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2, float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt, Object paramData);
	
	public default Object newPacket(String name, Object... params){
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
	
	public default Object getIChatBaseComponent(String text){
		try {
			return getNMSReflect().fromNameDotName("IChatBaseComponent", "ChatSerializer").getDeclaredMethod("b", String.class).invoke(null, text);
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return text;
	}
	
	public default Object getEnumChatFormat(int value){
		try {
			return getNMSReflect().fromName("EnumChatFormat").getDeclaredMethod("a", int.class).invoke(null, value);
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public default ReflectUtils getNMSReflect(){
		return ReflectUtils.fromPackage("net.minecraft.server." + getClass().getSimpleName());
	}
	
	public default ReflectUtils getCraftReflect(){
		return ReflectUtils.fromPackage("org.bukkit.craftbukkit." + getClass().getSimpleName());
	}
	
	public void sendPacket(Player p, Object packet);
    
    public static NMS getNMS(){
    	return BeautyQuests.nms;
    }
	
}
