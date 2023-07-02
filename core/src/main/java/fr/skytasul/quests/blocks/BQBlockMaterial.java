package fr.skytasul.quests.blocks;

import org.bukkit.block.Block;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.blocks.BQBlockOptions;

public class BQBlockMaterial extends BQBlock {

	private final XMaterial material;

	public BQBlockMaterial(BQBlockOptions options, XMaterial material) {
		super(options);
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