package fr.skytasul.quests.utils.nms;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;

public class NullNMS extends NMS {
	
	public Object bookPacket(ByteBuf buf) {
		throw new UnsupportedOperationException("Your version is not compatible.");
	}
	
	public Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2,
			float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt,
			Object paramData) {
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	
	public void sendPacket(Player p, Object packet) {
		throw new UnsupportedOperationException("Your version is not compatible.");
	}
	
	public ReflectUtils getReflect(){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	public double entityNameplateHeight(LivingEntity en){
		return en.getEyeHeight() + 1;
	}

	public Object getIChatBaseComponent(String text){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	public Object getEnumChatFormat(int value){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

}
