package fr.skytasul.quests.utils.nms;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.utils.ReflectUtils;

import io.netty.buffer.ByteBuf;

public class NullNMS extends NMS {
	
	@Override
	public Object bookPacket(ByteBuf buf) {
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	@Override
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		return meta1.equals(meta2);
	}
	
	@Override
	public void sendPacket(Player p, Object packet) {
		throw new UnsupportedOperationException("Your version is not compatible.");
	}
	
	public ReflectUtils getReflect(){
		throw new UnsupportedOperationException("Your version is not compatible.");
	}

	@Override
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
