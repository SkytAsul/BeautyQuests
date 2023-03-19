package fr.skytasul.quests.utils.nms;

import java.util.List;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import io.netty.buffer.ByteBuf;

public class v1_19_R3 extends NMS{
	
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
	public double entityNameplateHeight(Entity en) {
		return en.getHeight();
	}
	
	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		RegistryLookup<Block> blockRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK);
		Reference<Block> block = blockRegistry
				.getOrThrow(ResourceKey.create(Registries.BLOCK, new ResourceLocation(material.getKey().getKey())));
		StateDefinition<Block, BlockState> stateList = block.value().getStateDefinition();
		return stateList.getProperties().stream().map(Property::getName).toList();
	}
	
	@Override
	public List<String> getAvailableBlockTags() {
		return MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK).listTags()
				.map(x -> x.key().location().toString()).toList();
	}
	
}