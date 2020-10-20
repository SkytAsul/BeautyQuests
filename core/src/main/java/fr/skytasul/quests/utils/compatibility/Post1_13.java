package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import fr.skytasul.quests.utils.ParticleEffect.OrdinaryColor;

public class Post1_13 {

	public static boolean isItem(Material mat){
		return mat.isItem();
	}
	
	public static boolean isBlock(Material mat) {
		return mat.isBlock();
	}
	
	public static boolean isBlockData(Object data){
		return data instanceof BlockData;
	}
	
	public static boolean blockDataMatches(Object data, Block block) {
		return block.getBlockData().matches((BlockData) data);
	}
	
	public static String blockDataAsString(Object data) {
		return ((BlockData) data).getAsString();
	}
	
	public static BlockData createBlockData(String data) {
		return Bukkit.createBlockData(data);
	}
	
	public static Material blockDataGetMaterial(Object data) {
		return ((BlockData) data).getMaterial();
	}
	
	public static Object getDustColor(OrdinaryColor color, int size){
		return new Particle.DustOptions(Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed()), size);
	}
	
}
