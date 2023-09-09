package fr.skytasul.quests.blocks;

import java.util.*;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.blocks.BQBlockOptions;
import fr.skytasul.quests.api.blocks.BQBlockType;
import fr.skytasul.quests.api.blocks.BQBlocksManager;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.Located.LocatedBlock;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Post1_13;

public class BQBlocksManagerImplementation implements BQBlocksManager {

	public static final String BLOCKDATA_HEADER = "blockdata:";
	public static final String TAG_HEADER = "tag:";

	private final BiMap<String, BQBlockType> types = HashBiMap.create(5);

	private BQBlockType materialType;
	private BQBlockType blockdataType;
	private BQBlockType tagType;

	public BQBlocksManagerImplementation() {
		registerDefaultTypes();
	}

	private void registerDefaultTypes() {
		materialType = (string, options) -> new BQBlockMaterial(options, XMaterial.valueOf(string));
		registerBlockType("", materialType);

		if (MinecraftVersion.MAJOR >= 13) {
			blockdataType = (string, options) -> new Post1_13.BQBlockData(options, string);
			registerBlockType("blockdata:", blockdataType);

			tagType = (string, options) -> new Post1_13.BQBlockTag(options, string);
			registerBlockType("tag:", tagType);
		}
	}

	@Override
	public @NotNull BQBlock deserialize(@NotNull String string) throws IllegalArgumentException {
		BQBlockType type;

		String header = "";
		int separator = string.indexOf(HEADER_SEPARATOR);
		if (separator != -1) {
			header = string.substring(0, separator);
			string = string.substring(separator + 1);
		}
		type = types.get(header);

		if (type == null)
			throw new IllegalArgumentException("Unknown block header: " + header);

		int nameIndex = string.lastIndexOf(CUSTOM_NAME_FOOTER);
		String customName = nameIndex == -1 ? null : string.substring(nameIndex + CUSTOM_NAME_FOOTER.length());

		int dataEnd = nameIndex == -1 ? string.length() : nameIndex;

		return type.deserialize(string.substring(0, dataEnd), new BQBlockOptions(type, customName));
	}

	@Override
	public void registerBlockType(@Nullable String header, @NotNull BQBlockType type) {
		if (types.containsKey(header))
			throw new IllegalArgumentException("The block type with header " + header + " was already registered");

		types.put(header, type);
	}

	@Override
	public @Nullable String getHeader(@NotNull BQBlockType type) {
		return types.inverse().get(type);
	}

	@Override
	public @NotNull Spliterator<@NotNull Located> getNearbyBlocks(@NotNull Locatable.MultipleLocatable.NearbyFetcher fetcher,
			@NotNull Collection<BQBlock> types) {
		if (!fetcher.isTargeting(LocatedType.BLOCK))
			return Spliterators.emptySpliterator();

		int minY = (int) Math.max(fetcher.getCenter().getWorld().getMinHeight(),
				fetcher.getCenter().getY() - fetcher.getMaxDistance());
		double maxY = Math.min(fetcher.getCenter().getWorld().getMaxHeight(),
				fetcher.getCenter().getY() + fetcher.getMaxDistance());

		int centerX = fetcher.getCenter().getBlockX();
		int centerZ = fetcher.getCenter().getBlockZ();

		return Spliterators.spliteratorUnknownSize(new Iterator<Locatable.Located>() {

			int x = centerX;
			int z = centerZ;

			int i = 0;
			int y = minY;

			Locatable.Located.LocatedBlock found = null;

			private boolean findNext() {
				for (; y <= maxY; y++) {
					Block blockAt = fetcher.getCenter().getWorld().getBlockAt(x, y, z);
					if (types.stream().anyMatch(type -> type.applies(blockAt))) {
						found = Locatable.Located.LocatedBlock.create(blockAt);
						y++;
						return true;
					}
				}
				if (Math.abs(x - centerX) <= Math.abs(z - centerZ) && ((x - centerX) != (z - centerZ) || x >= centerX))
					x += ((z >= centerZ) ? 1 : -1);
				else
					z += ((x >= centerX) ? -1 : 1);

				i++;
				if (i >= fetcher.getMaxDistance() * fetcher.getMaxDistance())
					return false;
				y = minY;
				return findNext();

				// used the N spiral algorithm from here: https://stackoverflow.com/a/31864777
			}

			@Override
			public boolean hasNext() {
				if (found != null)
					return true;
				return findNext();
			}

			@Override
			public Located next() {
				if (found == null)
					findNext();
				if (found != null) {
					LocatedBlock tmpFound = found;
					found = null;
					return tmpFound;
				}
				throw new NoSuchElementException();
			}

		}, Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.NONNULL);
	}

	@Override
	public @NotNull BQBlock createSimple(@NotNull XMaterial material, @Nullable String customName) {
		return new BQBlockMaterial(new BQBlockOptions(materialType, customName), material);
	}

	public @NotNull BQBlock createBlockdata(@NotNull String blockData, @Nullable String customName) {
		return new Post1_13.BQBlockData(new BQBlockOptions(blockdataType, customName), blockData);
	}

	public @NotNull BQBlock createTag(@NotNull String tag, @Nullable String customName) {
		return new Post1_13.BQBlockTag(new BQBlockOptions(tagType, customName), tag);
	}

}
