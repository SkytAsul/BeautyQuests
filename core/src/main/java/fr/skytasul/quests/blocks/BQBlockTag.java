package fr.skytasul.quests.blocks;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.blocks.BQBlockOptions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import java.util.Set;

public class BQBlockTag extends BQBlock {

	private final Tag<Material> tag;
	private final String tagKey;

	public BQBlockTag(BQBlockOptions options, String stringData) {
		this(options, Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.fromString(stringData), Material.class));
	}

	public BQBlockTag(BQBlockOptions options, Tag<Material> tag) {
		super(options);
		this.tagKey = tag.getKey().toString();
		this.tag = tag;
	}

	@Override
	public boolean applies(Block block) {
		return tag != null && tag.isTagged(block.getType());
	}

	@Override
	public XMaterial retrieveMaterial() {
		if (tag != null) {
			Set<Material> values = tag.getValues();
			if (!values.isEmpty()) return XMaterial.matchXMaterial(values.iterator().next());
		}
		return XMaterial.BARRIER;
	}

	@Override
	public String getDefaultName() {
		return tag == null ? tagKey : tag.getKey().getKey();
	}

	@Override
	public String getDataString() {
		return tagKey;
	}

}