package fr.skytasul.quests.utils.types;

import org.bukkit.block.Block;

import fr.skytasul.quests.utils.XBlock;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Post1_13;

public class BQBlock {
	
	private static final String BLOCKDATA_HEADER = "blockdata:";
	
	private final XMaterial material;
	private final Object blockData;
	
	public BQBlock(XMaterial material) {
		this.material = material;
		this.blockData = null;
	}
	
	public BQBlock(Object blockData) {
		this.blockData = blockData;
		this.material = null;
	}
	
	public XMaterial getMaterial() {
		return material != null ? material : XMaterial.matchXMaterial(Post1_13.blockDataGetMaterial(blockData));
	}
	
	public boolean applies(Block block) {
		if (material != null) return XBlock.isType(block, material);
		return Post1_13.blockDataMatches(blockData, block);
	}
	
	public String getAsString() {
		if (material != null) return material.name();
		return BLOCKDATA_HEADER + Post1_13.blockDataAsString(blockData);
	}
	
	@Override
	public String toString() {
		return "BQBlock{" + getAsString() + "}";
	}
	
	public static BQBlock fromString(String string) {
		if (string.startsWith(BLOCKDATA_HEADER)) return new BQBlock(Post1_13.createBlockData(string.substring(BLOCKDATA_HEADER.length())));
		return new BQBlock(XMaterial.valueOf(string));
	}
	
}
