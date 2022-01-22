package fr.skytasul.quests.utils.compatibility;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import fr.skytasul.quests.utils.ParticleEffect.OrdinaryColor;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;

public class Post1_13 {

	public static boolean isItem(Material mat){
		return mat.isItem();
	}
	
	public static boolean isBlock(Material mat) {
		return mat.isBlock();
	}
	
	public static Object getDustColor(OrdinaryColor color, int size){
		return new Particle.DustOptions(Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed()), size);
	}
	
	public static boolean isBlockData(Object data) {
		return data instanceof BlockData;
	}
	
	public static class BQBlockData extends BQBlock {
		
		private final BlockData data;
		
		public BQBlockData(String stringData) {
			this.data = Bukkit.createBlockData(stringData);
		}
		
		public BQBlockData(BlockData data) {
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
		public String getAsString() {
			return BQBlock.BLOCKDATA_HEADER + data.getAsString();
		}
		
	}
	
	public static class BQBlockTag extends BQBlock {
		
		private final Tag<Material> tag;
		private final String tagKey;
		
		public BQBlockTag(String stringData) {
			this.tagKey = stringData;
			this.tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.fromString(stringData), Material.class);
		}
		
		public BQBlockTag(Tag<Material> tag) {
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
		public String getName() {
			return tag == null ? tagKey : tag.getKey().getKey();
		}
		
		@Override
		public String getAsString() {
			return BQBlock.TAG_HEADER + tagKey;
		}
		
	}
	
}
