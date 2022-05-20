package fr.skytasul.quests.utils.nms;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.EnumChatFormat;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketDataSerializer;
import net.minecraft.server.v1_11_R1.PacketPlayOutCustomPayload;

import io.netty.buffer.ByteBuf;

public class v1_11_R1 extends NMS{
	
	@Override
	public Object bookPacket(ByteBuf buf){
		return new PacketPlayOutCustomPayload("MC|BOpen", new PacketDataSerializer(buf));
	}
	
	@Override
	public void sendPacket(Player p, Object packet){
		Validate.isTrue(packet instanceof Packet, "The object specified is not a packet.");
		((CraftPlayer) p).getHandle().playerConnection.sendPacket((Packet<?>) packet);
	}

	@Override
	public double entityNameplateHeight(Entity en){
		return ((CraftEntity) en).getHandle().length;
	}

	public Object getIChatBaseComponent(String text){
		return IChatBaseComponent.ChatSerializer.b(text);
	}

	public Object getEnumChatFormat(int value){
		return EnumChatFormat.a(value);
	}
	
}