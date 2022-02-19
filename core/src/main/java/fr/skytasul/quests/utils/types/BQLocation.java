package fr.skytasul.quests.utils.types;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

public class BQLocation extends Location {
	
	private Pattern worldPattern;
	
	public BQLocation(Location bukkit) {
		this(bukkit.getWorld(), bukkit.getX(), bukkit.getY(), bukkit.getZ(), bukkit.getYaw(), bukkit.getPitch());
	}
	
	public BQLocation(World world, double x, double y, double z) {
		this(world, x, y, z, 0, 0);
	}
	
	public BQLocation(World world, double x, double y, double z, float yaw, float pitch) {
		super(world, x, y, z, yaw, pitch);
		if (world == null) worldPattern = Pattern.compile(".*");
	}
	
	public BQLocation(String worldPattern, double x, double y, double z) {
		this(worldPattern, x, y, z, 0, 0);
	}
	
	public BQLocation(String worldPattern, double x, double y, double z, float yaw, float pitch) {
		super(null, x, y, z, yaw, pitch);
		this.worldPattern = Pattern.compile(worldPattern);
	}
	
	public Pattern getWorldPattern() {
		return worldPattern;
	}
	
	public BQLocation setWorldPattern(Pattern worldPattern) {
		this.worldPattern = worldPattern;
		if (worldPattern != null) super.setWorld(null);
		return this;
	}
	
	@Override
	public void setWorld(World world) {
		throw new UnsupportedOperationException();
	}
	
	public boolean isWorld(World world) {
		Validate.notNull(world);
		if (super.getWorld() != null) return super.getWorld().equals(world);
		return worldPattern.matcher(world.getName()).matches();
	}
	
	public String getWorldName() {
		return getWorld() == null ? worldPattern.pattern() : getWorld().getName();
	}
	
	@Override
	public double distanceSquared(Location o) {
		Validate.isTrue(isWorld(o.getWorld()), "World does not match");
		return NumberConversions.square(getX() - o.getX())
				+ NumberConversions.square(getY() - o.getY())
				+ NumberConversions.square(getZ() - o.getZ());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) return false;
		BQLocation other = (BQLocation) obj;
		if (worldPattern == null) return other.worldPattern == null;
		if (other.worldPattern == null) return false;
		return worldPattern.pattern().equals(other.worldPattern.pattern());
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = 19 * hash + (worldPattern == null ? 0 : worldPattern.pattern().hashCode());
		return hash;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();
		
		if (!map.containsKey("world"))
			map.put("pattern", worldPattern.pattern());
		
		return map;
	}
	
	@NotNull
	public static BQLocation deserialize(@NotNull Map<String, Object> args) {
		double x = NumberConversions.toDouble(args.get("x"));
		double y = NumberConversions.toDouble(args.get("y"));
		double z = NumberConversions.toDouble(args.get("z"));
		float yaw = NumberConversions.toFloat(args.get("yaw"));
		float pitch = NumberConversions.toFloat(args.get("pitch"));
		
		World world = null;
		String worldPattern = null;
		
        if (args.containsKey("world")) {
			String worldName = (String) args.get("world");
			world = Bukkit.getWorld(worldName);
			if (world == null) worldPattern = Pattern.quote(worldName);
		}else if (args.containsKey("pattern")) {
			worldPattern = (String) args.get("pattern");
		}
		
		if (worldPattern != null) return new BQLocation(worldPattern, x, y, z, yaw, pitch);
		return new BQLocation(world, x, y, z, yaw, pitch);
    }
	
}
