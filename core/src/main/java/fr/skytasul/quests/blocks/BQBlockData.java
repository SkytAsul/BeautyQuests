package fr.skytasul.quests.blocks;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.blocks.BQBlockOptions;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class BQBlockData extends BQBlock {

	private final BlockData data;

	public BQBlockData(BQBlockOptions options, BlockData data) {
		super(options);
		this.data = data;
	}

	@Override
	public boolean applies(Block block) {
		return block.getBlockData().matches(data);
	}

	@Override
	public XMaterial retrieveMaterial() {
		return XMaterial.matchXMaterial(data.getMaterial());
	}

	@Override
	public String getDataString() {
		return data.getAsString(true);
	}

}