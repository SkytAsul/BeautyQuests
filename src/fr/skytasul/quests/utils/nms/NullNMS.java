package fr.skytasul.quests.utils.nms;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ReflectUtils;
import io.netty.buffer.ByteBuf;

public class NullNMS implements NMS {

	
	public Object bookPacket(ByteBuf buf) {
		return null;
	}

	
	public Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2,
			float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt,
			Object paramData) {
		return null;
	}

	
	public void sendPacket(Player p, Object packet) {
	}
	
	
	public ReflectUtils getReflect(){
		return null;
	}

}
