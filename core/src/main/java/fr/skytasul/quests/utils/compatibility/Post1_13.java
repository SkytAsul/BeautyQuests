package fr.skytasul.quests.utils.compatibility;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.blocks.BQBlockOptions;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import java.util.Set;

// TODO delete, no longer useful
public class Post1_13 {

	public static boolean isItem(Material mat){
		return mat.isItem();
	}

	public static boolean isBlock(Material mat) {
		return mat.isBlock();
	}

	public static Class<?> getDustOptionClass() {
		return Particle.DustOptions.class;
	}

	public static Object getDustColor(Color color, int size) {
		return new Particle.DustOptions(color, size);
	}

	public static boolean isBlockData(Object data) {
		return data instanceof BlockData;
	}

	public static class BQBlockData extends BQBlock {

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

	public static class BQBlockTag extends BQBlock {

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

}
