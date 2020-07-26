package fr.skytasul.quests.utils.types;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import fr.skytasul.quests.utils.XBlock;
import fr.skytasul.quests.utils.XMaterial;

public class BQBlock {
	
	private static final String BLOCKDATA_HEADER = "blockdata:";
	
	private final XMaterial material;
	private final BlockData blockData;
	
	public BQBlock(XMaterial material) {
		this.material = material;
		this.blockData = null;
	}
	
	public BQBlock(BlockData blockData) {
		this.blockData = blockData;
		this.material = null;
	}
	
	public XMaterial getMaterial() {
		return blockData == null ? material : XMaterial.matchXMaterial(blockData.getMaterial());
	}
	
	public boolean applies(Block block) {
		if (material != null) return XBlock.isType(block, material);
		return blockData.matches(block.getBlockData());
	}
	
	public String getAsString() {
		if (material != null) return material.name();
		return BLOCKDATA_HEADER + blockData.getAsString(true);
	}
	
	public static BQBlock fromString(String string) {
		if (string.startsWith(BLOCKDATA_HEADER)) return new BQBlock(Bukkit.createBlockData(string.substring(BLOCKDATA_HEADER.length())));
		return new BQBlock(XMaterial.valueOf(string));
	}
	
}
