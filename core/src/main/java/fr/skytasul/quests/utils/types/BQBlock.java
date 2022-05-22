package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.block.Block;

import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.Located;
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
	
	public static Collection<Locatable.Located> getNearbyBlocks(Locatable.MultipleLocatable.NearbyFetcher fetcher, Collection<BQBlock> types) {
		if (!fetcher.getTargetClass().isAssignableFrom(Located.LocatedBlock.class)) return Collections.emptyList();
		List<Locatable.Located> blocks = new ArrayList<>();
		
		int minY = (int) Math.max(fetcher.getCenter().getWorld().getMinHeight(), fetcher.getCenter().getY() - fetcher.getMaxDistance());
		double maxY = Math.min(fetcher.getCenter().getWorld().getMaxHeight(), fetcher.getCenter().getY() + fetcher.getMaxDistance());
		
		int centerX = fetcher.getCenter().getBlockX();
		int centerZ = fetcher.getCenter().getBlockZ();
		int x = centerX;
		int z = centerZ;
		spiral: for (int i = 0; i < fetcher.getMaxDistance() * fetcher.getMaxDistance(); i++) {
			if (!fetcher.getCenter().getWorld().isChunkLoaded(x >> 4, z >> 4)) continue;
			for (int y = minY; y <= maxY; y++) {
				Block blockAt = fetcher.getCenter().getWorld().getBlockAt(x, y, z);
				if (types.stream().anyMatch(type -> type.applies(blockAt))) {
					blocks.add(Locatable.Located.LocatedBlock.create(blockAt));
					if (blocks.size() >= fetcher.getMaxAmount()) break spiral;
				}
			}
			if (Math.abs(x - centerX) <= Math.abs(z - centerZ) && ((x - centerX) != (z - centerZ) || x >= centerX))
				x += ((z >= centerZ) ? 1 : -1);
			else
				z += ((x >= centerX) ? -1 : 1);
		}
		
		// used the N spiral algorithm from here: https://stackoverflow.com/a/31864777
		return blocks;
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
			return XBlock.isType(block, material);
		}
		
		@Override
		public String getAsString() {
			return material.name();
		}
		
	}
	
}
