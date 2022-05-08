package fr.skytasul.quests.utils.nms;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import io.netty.buffer.ByteBuf;

public class v1_18_R1 extends NMS{
	
	@Override
	public Object bookPacket(ByteBuf buf){
		return new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);
	}
	
	@Override
	public void sendPacket(Player p, Object packet){
		Validate.isTrue(packet instanceof Packet, "The object specified is not a packet.");
		((CraftPlayer) p).getHandle().connection.send((Packet<?>) packet);
	}

	@Override
	public double entityNameplateHeight(LivingEntity en){
		return en.getHeight();
	}
	
	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		Block block = Registry.BLOCK.get(new ResourceLocation(material.getKey().getKey()));
		StateDefinition<Block, BlockState> stateList = block.getStateDefinition();
		return stateList.getProperties().stream().map(Property::getName).toList();
	}
	
	@Override
	public List<String> getAvailableBlockTags() {
		return BlockTags.getAllTags().getAllTags().keySet().stream().map(ResourceLocation::toString).toList();
	}
	
}