package fr.skytasul.quests.utils.nms;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Field;
import java.util.List;

public class v1_21_R1 extends NMS{

	private Field customTagField;

	public v1_21_R1() throws NoSuchFieldException, SecurityException {
		customTagField = craftReflect.fromName("inventory.CraftMetaItem").getDeclaredField("customTag");
		customTagField.setAccessible(true);
	}

	@Override
	public void openBookInHand(Player p) {
		ClientboundOpenBookPacket packet = new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);
		((CraftPlayer) p).getHandle().connection.send(packet);
	}

	@Override
	public double entityNameplateHeight(Entity en) {
		return en.getHeight();
	}

	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		RegistryLookup<Block> blockRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK);
		Reference<Block> block = blockRegistry
				.getOrThrow(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse(material.getKey().toString())));
		StateDefinition<Block, BlockState> stateList = block.value().getStateDefinition();
		return stateList.getProperties().stream().map(Property::getName).toList();
	}

	@Override
	public List<String> getAvailableBlockTags() {
		return MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK).listTags()
				.map(x -> x.key().location().toString()).toList();
	}

	@Override
	public boolean equalsWithoutNBT(ItemMeta meta1, ItemMeta meta2) throws ReflectiveOperationException {
		unhandledTags.set(meta1, DataComponentPatch.builder());
		unhandledTags.set(meta2, DataComponentPatch.builder());
		customTagField.set(meta1, null);
		customTagField.set(meta2, null);
		return (boolean) equalsCommon.invoke(meta1, meta2);
	}

}
