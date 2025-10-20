package fr.skytasul.quests.utils.nms;

import net.kyori.adventure.key.Key;
import java.util.List;
import org.bukkit.Material;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class v1_20_R2 extends NMS{

	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		RegistryLookup<Block> blockRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK);
		Reference<Block> block = blockRegistry
				.getOrThrow(ResourceKey.create(Registries.BLOCK, new ResourceLocation(material.getKey().getKey())));
		StateDefinition<Block, BlockState> stateList = block.value().getStateDefinition();
		return stateList.getProperties().stream().map(Property::getName).toList();
	}

	@Override
	public List<Key> getAvailableBlockTags() {
		return MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK).listTags()
				.map(x -> Key.key(x.key().location().toString())).toList();
	}

}
