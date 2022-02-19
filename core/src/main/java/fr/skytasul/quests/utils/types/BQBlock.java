package fr.skytasul.quests.utils.types;

import org.bukkit.block.Block;

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
