package fr.skytasul.quests.utils;

import java.util.List;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.compatibility.Post1_13;
import fr.skytasul.quests.utils.nms.NMS;

public class ParticleEffect {
	
	private static Random random = new Random();
	
	private ParticleType type;
	private ParticleShape shape;
	
	private Color color;
	private double[] colors;
	private Object dustColor;
	
	public ParticleEffect(Particle bukkitType, ParticleShape shape, Color color) {
		this.type = new ParticleType(bukkitType);
		this.shape = shape;
		this.color = color;
		
		if (type.dustColored) {
			dustColor = Post1_13.getDustColor(color, 1);
		}else if (type.colored && bukkitType != Particle.NOTE) {
			colors = new double[3];
			colors[0] = color.getRed() == 0 ? Float.MIN_NORMAL : color.getRed() / 255D;
			colors[1] = color.getGreen() / 255D;
			colors[2] = color.getBlue() / 255D;
		}
	}
	
	@Override
	public String toString() {
		return type.particle.name() + " in shape " + shape.name() + (type.colored ? " with color \"R" + (type.particle != Particle.NOTE ? color.getRed() + " G" + color.getGreen() + " B" + color.getBlue() : "random") + "\"" : "");
	}
	
	public void send(LivingEntity entity, List<Player> players) {
		send(entity.getLocation(), NMS.getNMS().entityNameplateHeight(entity), players);
	}
	
	public void send(Location bottom, double height, List<Player> players) {
		if (players.isEmpty()) return;
		switch (shape) {
		case POINT:
			sendParticle(bottom.add(0, height + 0.7, 0), players, 0.01, 0.01, 0.01, 2);
			break;
		case NEAR:
			sendParticle(bottom.add(0, height / 2, 0), players, 0.45, height / 2 + 0.1, 0.45, 1);
			break;
		case BAR:
			sendParticle(bottom.add(0, height + 0.7, 0), players, 0.01, 0.15, 0.01, 3);
			break;
		case EXCLAMATION:
			sendParticle(bottom.add(0, height + 0.7, 0), players, 0, 0, 0, 1); //	POINT
			sendParticle(bottom.add(0, 0.65, 0), players, 0, 0.2, 0, 5); //	BAR
			break;
		case SPOT:
			sendParticle(bottom.add(0, height, 0), players, 0.2, 0.4, 0.2, 15);
			break;
		}
	}
	
	public void sendParticle(Location lc, List<Player> players, double offX, double offY, double offZ, int amount) {
		double extra = 0.001;
		Object data = null;
		if (type.particle == Particle.NOTE) {
			offX = random.nextInt(24) / 24D; // this offset contains the note number
			offY = 0;
			offZ = 0;
		}else if (dustColor != null) {
			data = dustColor;
		}else if (colors != null) {
			// in MC < 1.13, color was passed as offsets.
			offX = colors[0];
			offY = colors[1];
			offZ = colors[2];
			amount = 0; // amount must be 0 for colors to be enabled
			extra = 1; // the extra field controls the brightness
		}
		
		for (Player p : players) {
			p.spawnParticle(type.particle, lc, amount, offX, offY, offZ, extra, data);
		}
	}
	
	public static ParticleEffect deserialize(ConfigurationSection data) {
		return new ParticleEffect(
				Particle.valueOf(data.getString("particleEffect").toUpperCase()),
				ParticleShape.valueOf(data.getString("particleShape").toUpperCase()),
				Color.deserialize(data.getConfigurationSection("particleColor").getValues(false)));
	}
	
	public enum ParticleShape {
		POINT, NEAR, BAR, EXCLAMATION, SPOT;
	}
	
	private static class ParticleType {
		
		private final Particle particle;
		private final boolean colored;
		private final boolean dustColored;
		
		private ParticleType(Particle particle) {
			this.particle = particle;
			
			if (particle == Particle.NOTE) {
				colored = true;
				dustColored = false;
			}else if (NMS.getMCVersion() >= 13) {
				if (particle.getDataType() == Post1_13.getDustOptionClass()) {
					colored = true;
					dustColored = true;
				}else {
					colored = false;
					dustColored = false;
				}
			}else {
				colored = particle == Particle.REDSTONE || particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT;
				dustColored = false;
			}
			
			if (particle.getDataType() != Void.class && !dustColored) throw new IllegalArgumentException("Particle type " + particle.name() + " must have a " + particle.getDataType().getName() + " data");
		}
		
	}
	
}