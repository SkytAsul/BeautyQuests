package fr.skytasul.quests.utils.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;

import org.bukkit.block.Block;

import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
import fr.skytasul.quests.api.stages.types.Locatable.Located.LocatedBlock;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XBlock;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Post1_13;

public abstract class BQBlock {
	
	public static final String BLOCKDATA_HEADER = "blockdata:";
	public static final String TAG_HEADER = "tag:";
	
	private XMaterial cachedMaterial;
	
	public abstract String getAsString();
	
	public abstract boolean applies(Block block);
	
	public abstract XMaterial retrieveMaterial();
	
	public final XMaterial getMaterial() {
		if (cachedMaterial == null) cachedMaterial = retrieveMaterial();
		return cachedMaterial;
	}
	
	public String getName() {
		return MinecraftNames.getMaterialName(getMaterial());
	}
	
	@Override
	public String toString() {
		return "BQBlock{" + getAsString() + "}";
	}
	
	public static BQBlock fromString(String string) {
		if (string.startsWith(BLOCKDATA_HEADER)) return new Post1_13.BQBlockData(string.substring(BLOCKDATA_HEADER.length()));
		if (string.startsWith(TAG_HEADER)) return new Post1_13.BQBlockTag(string.substring(TAG_HEADER.length()));
		return new BQBlockMaterial(XMaterial.valueOf(string));
	}
	
	public static Spliterator<Locatable.Located> getNearbyBlocks(Locatable.MultipleLocatable.NearbyFetcher fetcher, Collection<BQBlock> types) {
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
			
		}, Spliterator.ORDERED & Spliterator.IMMUTABLE & Spliterator.DISTINCT & Spliterator.NONNULL);
	}
	
	public static class BQBlockMaterial extends BQBlock {
		
		private final XMaterial material;
		
		public BQBlockMaterial(XMaterial material) {
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
		public String getAsString() {
			return material.name();
		}
		
	}
	
}
