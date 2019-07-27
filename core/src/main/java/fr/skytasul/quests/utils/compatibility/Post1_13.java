package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;

import fr.skytasul.quests.utils.ParticleEffect.OrdinaryColor;

public class Post1_13 {

	public static boolean isItem(Material mat){
		return mat.isItem();
	}
	
	public static boolean equalBlockData(Object data){
		return data instanceof BlockData;
	}
	
	public static Object getDustColor(OrdinaryColor color, int size){
		return new Particle.DustOptions(Color.fromBGR(color.getBlue(), color.getGreen(), color.getRed()), size);
	}
	
}
