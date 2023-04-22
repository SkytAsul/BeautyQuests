package fr.skytasul.quests.utils.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.Located.LocatedBlock;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.compatibility.Post1_13;

public abstract class BQBlock {
	
	public static final String BLOCKDATA_HEADER = "blockdata:";
	public static final String TAG_HEADER = "tag:";
	public static final String CUSTOM_NAME_FOOTER = "|customname:";
	
	private final @Nullable String customName;

	private @Nullable XMaterial cachedMaterial;
	private @Nullable String cachedName;
	
	protected BQBlock(@Nullable String customName) {
		this.customName = customName;
	}

	protected abstract @NotNull String getDataString();
	
	public abstract boolean applies(@NotNull Block block);
	
	public abstract @NotNull XMaterial retrieveMaterial();
	
	public final @NotNull XMaterial getMaterial() {
		if (cachedMaterial == null) cachedMaterial = retrieveMaterial();
		return cachedMaterial;
	}
	
	public @NotNull String getDefaultName() {
		return MinecraftNames.getMaterialName(getMaterial());
	}

	public final @NotNull String getName() {
		if (cachedName == null)
			cachedName = customName == null ? getDefaultName() : customName;

		return cachedName;
	}

	public final @NotNull String getAsString() {
		return getDataString() + getFooter();
	}
	
	private @NotNull String getFooter() {
		return customName == null ? "" : CUSTOM_NAME_FOOTER + customName;
	}

	@Override
	public String toString() {
		return "BQBlock{" + getAsString() + "}";
	}

	public static @NotNull BQBlock fromString(@NotNull String string) {
		int nameIndex = string.lastIndexOf(CUSTOM_NAME_FOOTER);
		String customName = nameIndex == -1 ? null : string.substring(nameIndex + CUSTOM_NAME_FOOTER.length());

		int dataEnd = nameIndex == -1 ? string.length() : nameIndex;

		if (string.startsWith(BLOCKDATA_HEADER))
			return new Post1_13.BQBlockData(customName, string.substring(BLOCKDATA_HEADER.length(), dataEnd));
		if (string.startsWith(TAG_HEADER))
			return new Post1_13.BQBlockTag(customName, string.substring(TAG_HEADER.length(), dataEnd));
		return new BQBlockMaterial(customName, XMaterial.valueOf(string.substring(0, dataEnd)));
	}
	
	public static @NotNull Spliterator<Locatable.@NotNull Located> getNearbyBlocks(
			@NotNull Locatable.MultipleLocatable.NearbyFetcher fetcher, @NotNull Collection<@NotNull BQBlock> types) {
		if (!fetcher.isTargeting(LocatedType.BLOCK)) return Spliterators.emptySpliterator();
		
		int minY = (int) Math.max(fetcher.getCenter().getWorld().getMinHeight(), fetcher.getCenter().getY() - fetcher.getMaxDistance());
		double maxY = Math.min(fetcher.getCenter().getWorld().getMaxHeight(), fetcher.getCenter().getY() + fetcher.getMaxDistance());
		
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
				if (i >= fetcher.getMaxDistance() * fetcher.getMaxDistance()) return false;
				y = minY;
				return findNext();
				
				// used the N spiral algorithm from here: https://stackoverflow.com/a/31864777
			}
			
			@Override
			public boolean hasNext() {
				if (found != null) return true;
				return findNext();
			}
			
			@Override
			public Located next() {
				if (found == null) findNext();
				if (found != null) {
					LocatedBlock tmpFound = found;
					found = null;
					return tmpFound;
				}
				throw new NoSuchElementException();
			}
			
		}, Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.NONNULL);
	}
	
	public static class BQBlockMaterial extends BQBlock {
		
		private final XMaterial material;
		
		public BQBlockMaterial(String customName, XMaterial material) {
			super(customName);
			this.material = material;
		}
		
		@Override
		public XMaterial retrieveMaterial() {
			return material;
		}
		
		@Override
		public boolean applies(Block block) {
			return XBlock.isSimilar(block, material);
		}
		
		@Override
		public String getDataString() {
			return material.name();
		}
		
	}
	
}
