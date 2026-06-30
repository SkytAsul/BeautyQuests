package fr.skytasul.quests.utils.nms;

import fr.skytasul.quests.api.QuestsPlugin;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class PaperNMS extends NMS {

	private Field customTagField;

	private Method identifierFactory;
	private Constructor<?> identifierConstructor;
	private Method resourceKeyFactory;

	public PaperNMS() throws ReflectiveOperationException {
		customTagField = craftReflect.fromName("inventory.CraftMetaItem").getDeclaredField("customTag");
		customTagField.setAccessible(true);

		Class<?> identifierClass = Class.forName(
				QuestsPlugin.getPlugin().getServerVersion().isAfter(1, 21, 11) ? "net.minecraft.resources.Identifier"
				: "net.minecraft.resources.ResourceLocation");
		resourceKeyFactory = ResourceKey.class.getDeclaredMethod("create", ResourceKey.class, identifierClass);
		if (QuestsPlugin.getPlugin().getServerVersion().isAfter(1, 21, 0))
			identifierFactory = identifierClass.getDeclaredMethod("parse", String.class);
		else
			identifierConstructor = identifierClass.getDeclaredConstructor(String.class);
	}

	@SuppressWarnings("unchecked")
	private <T> ResourceKey<T> createResourceKey(ResourceKey<Registry<T>> registryKey, NamespacedKey key) {
		try {
			Object identifier;
			identifier = identifierFactory == null
					? identifierConstructor.newInstance(key.toString())
					: identifierFactory.invoke(null, key.toString());
			return (ResourceKey<T>) resourceKeyFactory.invoke(null, registryKey, identifier);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public List<String> getAvailableBlockProperties(Material material) {
		RegistryLookup<Block> blockRegistry = MinecraftServer.getServer().registryAccess().lookupOrThrow(Registries.BLOCK);
		Reference<Block> block = blockRegistry.getOrThrow(createResourceKey(Registries.BLOCK, material.getKey()));
		StateDefinition<Block, BlockState> stateList = block.value().getStateDefinition();
		return stateList.getProperties().stream().map(Property::getName).toList();
	}

	@Override
	public List<Key> getAvailableBlockTags() {
		return RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).getTags().stream()
				.map(x -> x.tagKey().key()).toList();
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
