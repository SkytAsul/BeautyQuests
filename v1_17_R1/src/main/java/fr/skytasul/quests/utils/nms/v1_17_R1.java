package fr.skytasul.quests.utils.nms;

import net.kyori.adventure.key.Key;
import java.util.List;
import org.bukkit.Material;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class v1_17_R1 extends NMS{
	
	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		Block block = Registry.BLOCK.get(new ResourceLocation(material.getKey().getKey()));
		StateDefinition<Block, BlockState> stateList = block.getStateDefinition();
		return stateList.getProperties().stream().map(Property::getName).toList();
	}
	
	@Override
	public List<Key> getAvailableBlockTags() {
		return BlockTags.getAllTags().getAllTags().keySet().stream().map(location -> Key.key(location.toString())).toList();
	}
	
}
