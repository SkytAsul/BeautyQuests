package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class BQLocation extends Location implements Locatable.Located, HasPlaceholders {

	private @Nullable Pattern worldPattern;

	private @Nullable PlaceholderRegistry placeholders;

	public BQLocation(@NotNull Location bukkit) {
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

	@Override
	public Location getLocation() {
		return new Location(getWorld(), getX(), getY(), getZ());
	}

	@Override
	public LocatedType getType() {
		return LocatedType.OTHER;
	}

	public boolean isWorld(World world) {
		Validate.notNull(world);
		if (super.getWorld() != null) return super.getWorld().equals(world);
		return worldPattern.matcher(world.getName()).matches();
	}

	public String getWorldName() {
		return getWorld() == null ? worldPattern.pattern() : getWorld().getName();
	}

	@Nullable
	public Block getMatchingBlock() {
		if (super.getWorld() != null) return super.getBlock();
		if (worldPattern == null) return null;
		return Bukkit.getWorlds()
					.stream()
					.filter(world -> worldPattern.matcher(world.getName()).matches())
					.findFirst()
					.map(world -> world.getBlockAt(getBlockX(), getBlockY(), getBlockZ()))
					.orElse(null);
	}

	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		if (placeholders == null)
			placeholders = new PlaceholderRegistry()
					.register("x", () -> Integer.toString(getBlockX()))
					.register("y", () -> Integer.toString(getBlockY()))
					.register("z", () -> Integer.toString(getBlockZ()))
					.register("world", () -> getWorldName())
					.register("world_name", () -> getWorld() == null ? null : getWorld().getName())
					.register("world_pattern", () -> worldPattern.pattern());
		return placeholders;
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
		if (!(obj instanceof Location)) return false;
		Location other = (Location) obj;

		if (Double.doubleToLongBits(this.getX()) != Double.doubleToLongBits(other.getX())) return false;
        if (Double.doubleToLongBits(this.getY()) != Double.doubleToLongBits(other.getY())) return false;
        if (Double.doubleToLongBits(this.getZ()) != Double.doubleToLongBits(other.getZ())) return false;
        if (Float.floatToIntBits(this.getPitch()) != Float.floatToIntBits(other.getPitch())) return false;
        if (Float.floatToIntBits(this.getYaw()) != Float.floatToIntBits(other.getYaw())) return false;

		if (obj instanceof BQLocation) {
			BQLocation otherBQ = (BQLocation) obj;
			if (worldPattern == null) return otherBQ.worldPattern == null;
			if (otherBQ.worldPattern == null) return false;
			return worldPattern.pattern().equals(otherBQ.worldPattern.pattern());
		}

		if (!Objects.equals(other.getWorld(), getWorld())) {
			if (other.getWorld() == null) return false;
			if (worldPattern == null) return false;
			return worldPattern.matcher(other.getWorld().getName()).matches();
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = 19 * hash + (worldPattern == null ? 0 : worldPattern.pattern().hashCode());
		return hash;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		if (getWorld() == null) {
			map.put("pattern", worldPattern.pattern());
		} else {
			map.put("world", getWorld().getName());
		}

		// we cannot use Location#serialize() to add the following values
		// because on 1.8 it will throw an NPE if the world is null

		map.put("x", getX());
		map.put("y", getY());
		map.put("z", getZ());

		map.put("yaw", getYaw());
		map.put("pitch", getPitch());

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
