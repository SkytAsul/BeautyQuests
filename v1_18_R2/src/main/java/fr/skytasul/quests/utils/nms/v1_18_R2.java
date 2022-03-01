package fr.skytasul.quests.utils.nms;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.CraftParticle;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.ParticleEffect;

import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import io.netty.buffer.ByteBuf;

public class v1_18_R2 extends NMS{
	
	@Override
	public Object bookPacket(ByteBuf buf){
		return new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);
	}

	@Override
	public Object worldParticlePacket(ParticleEffect effect, boolean paramBoolean, float paramFloat1, float paramFloat2,
			float paramFloat3, float paramFloat4, float paramFloat5, float paramFloat6, float paramFloat7, int paramInt,
			Object paramData) {
		return new ClientboundLevelParticlesPacket(CraftParticle.toNMS(effect.getBukkitParticle(), paramData), paramBoolean, paramFloat1, paramFloat2, paramFloat3, paramFloat4, paramFloat5, paramFloat6, paramFloat7, paramInt);
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
		return Registry.BLOCK.getTags().map(x -> x.getFirst().location().toString()).toList();
	}
	
}