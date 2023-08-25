package fr.skytasul.quests.api.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;

/**
 * A bunch of static methods who can be useful
 * @author SkytAsul
 */
public class Utils{

	public static Optional<String> getFilenameExtension(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}

	public static String ticksToElapsedTime(int ticks) {
		int i = ticks / 20;
		int j = i / 60;
		i = i % 60;
		return i < 10 ? j + ":0" + i : j + ":" + i;
	}

	public static String millisToHumanString(long time) {
		if (time == 0) return "x";

		StringBuilder sb = new StringBuilder();

		long weeks = time / 604_800_000;
		if (weeks != 0)
			sb.append(Lang.TimeWeeks.quickFormat("weeks_amount", weeks));
		time -= weeks * 604_800_000;

		long days = time / 86_400_000;
		if (sb.length() != 0) sb.append(' ');
		if (days != 0)
			sb.append(Lang.TimeDays.quickFormat("days_amount", days));
		time -= days * 86_400_000;

		long hours = time / 3_600_000;
		if (sb.length() != 0) sb.append(' ');
		if (hours != 0)
			sb.append(Lang.TimeHours.quickFormat("hours_amount", hours));
		time -= hours * 3_600_000;

		long minutes = time / 60_000;
		if (sb.length() != 0) sb.append(' ');
		if (minutes != 0)
			sb.append(Lang.TimeMinutes.quickFormat("minutes_amount", minutes));
		time -= minutes * 60_000;

		if (sb.length() == 0) sb.append(Lang.TimeLessMinute.toString());

		return sb.toString();
	}

	public static String getStringFromItemStack(ItemStack is, String amountColor, boolean showXOne) {
		return ItemUtils.getName(is, true) + ((is.getAmount() > 1 || showXOne) ? "§r" + amountColor + " x" + is.getAmount() : "");
	}

	public static void giveItems(Player p, List<ItemStack> items) {
		HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(items.stream().map(ItemStack::clone).toArray(ItemStack[]::new));
		if (!leftover.isEmpty()) {
			leftover.values().forEach(item -> p.getWorld().dropItem(p.getLocation(), item));
			Lang.ITEM_DROPPED.send(p);
		}
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		if (value == null)
			return null;
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static <T, E> List<T> getKeysByValue(Map<T, E> map, E value) {
		if (value == null) return Collections.emptyList();
		List<T> list = new ArrayList<>();
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				list.add(entry.getKey());
			}
		}
		return list;
	}

	public static long parseLong(Object obj) {
		if (obj instanceof Number) return ((Number) obj).longValue();
		if (obj instanceof String) return Long.parseLong((String) obj);
		return 0;
	}

	public static void walkResources(Class<?> clazz, String path, int depth, Consumer<Path> consumer) throws URISyntaxException, IOException {
		URI uri = clazz.getResource(path).toURI();
		FileSystem fileSystem = null;
		Path myPath;
		try {
			if (uri.getScheme().equals("jar")) {
				fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
				myPath = fileSystem.getPath(path);
			}else {
				myPath = Paths.get(uri);
			}

			try (Stream<Path> walker = Files.walk(myPath, depth)) {
				walker.forEach(consumer);
			}
		}finally {
			if (fileSystem != null) fileSystem.close();
		}
	}

	public static <T> List<Map<String, Object>> serializeList(Collection<T> objects, Function<T, Map<String, Object>> serialize) {
		List<Map<String, Object>> ls = new ArrayList<>();
		for (T obj : objects){
			ls.add(serialize.apply(obj));
		}
		return ls;
	}

	public static <T> List<T> deserializeList(List<Map<?, ?>> serialized, Function<Map<String, Object>, T> deserialize) {
		List<T> ls = new ArrayList<>();
		if (serialized != null) {
			for (Map<?, ?> map : serialized) {
				ls.add(deserialize.apply((Map<String, Object>) map));
			}
		}
		return ls;
	}

	public static Map<String, Object> mapFromConfigurationSection(ConfigurationSection section){
		Map<String, Object> map = section.getValues(true);
		for (Entry<String, Object> entry : section.getValues(true).entrySet()) {
			if (entry.getValue() instanceof ConfigurationSection) {
				map.put(entry.getKey(), mapFromConfigurationSection((ConfigurationSection) entry.getValue()));
			}else map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	public static ConfigurationSection createConfigurationSection(Map<String, Object> content) {
		MemoryConfiguration section = new MemoryConfiguration();
		setConfigurationSectionContent(section, content);
		return section;
	}

	public static void setConfigurationSectionContent(ConfigurationSection section, Map<String, Object> content) {
		content.forEach((key, value) -> {
			if (value instanceof Map) {
				section.createSection(key, (Map<?, ?>) value);
			}else {
				section.set(key, value);
			}
		});
	}

	public static List<ItemStack> combineItems(List<ItemStack> items) {
		ArrayList<ItemStack> newItems = new ArrayList<>(items.size());
		items: for (ItemStack original : items) {
			for (ItemStack newItem : newItems) {
				if (newItem.isSimilar(original)) {
					newItem.setAmount(newItem.getAmount() + original.getAmount());
					continue items;
				}
			}
			newItems.add(original.clone());
		}
		newItems.trimToSize();
		return newItems;
	}

	public static List<ItemStack> extractItems(List<ItemStack> items) {
		List<ItemStack> newItems = new ArrayList<>(items.size());
		for (ItemStack original : items) {
			int amount = original.getAmount();
			int maxStackSize = /*original.getMaxStackSize()*/ 64;
			while (amount > maxStackSize) {
				ItemStack item = original.clone();
				item.setAmount(maxStackSize);
				amount -= maxStackSize;
				newItems.add(item);
			}
			if (amount > 0) {
				ItemStack item = original.clone();
				item.setAmount(amount);
				newItems.add(item);
			}
		}
		return newItems;
	}

	private static SimpleDateFormat cachedFormat = new SimpleDateFormat("yyyyMMddHHmmss");;
	public static DateFormat getDateFormat(){
		return cachedFormat;
	}

	public static String convertLocationToString(Location loc) {
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		if ((int) loc.getPitch() != 0) {
			int yaw = (int) loc.getYaw();
			int pitch = (int) loc.getPitch();
			return world + " " + x + " " + y + " " + z + " " + yaw + " " + pitch;
		}
		return world + " " + x + " " + y + " " + z;
	}

	public static Location convertStringToLocation(String loc) {
		if (loc != null) {
			String[] coords = loc.split(" ");
			World w = Bukkit.getWorld(coords[0]);
			double x = Double.parseDouble(coords[1]);
			double y = Double.parseDouble(coords[2]);
			double z = Double.parseDouble(coords[3]);
			if (coords.length == 6) {
				float yaw = Float.parseFloat(coords[4]);
				float pitch = Float.parseFloat(coords[5]);
				return new Location(w, x, y, z, yaw, pitch);
			}
			return new Location(w, x, y, z);
		}
		return null;
	}

	public static boolean isQuestItem(ItemStack item) {
		if (item == null) return false;
		String lore = Lang.QuestItemLore.toString();
		if (!lore.isEmpty() && item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta.hasLore() && meta.getLore().contains(lore)) return true;
		}
		return false;
	}

	public static XMaterial mobItem(EntityType type) {
		if (type == null) return XMaterial.SPONGE;
		Optional<XMaterial> material = XMaterial.matchXMaterial(type.name() + "_SPAWN_EGG");
		if (material.isPresent() && material.get().isSupported())
			return material.get();
		if (type == EntityType.WITHER) return XMaterial.WITHER_SKELETON_SKULL;
		if (type == EntityType.IRON_GOLEM) return XMaterial.IRON_BLOCK;
		if (type == EntityType.SNOWMAN) return XMaterial.SNOW_BLOCK;
		if (type == EntityType.MUSHROOM_COW) return XMaterial.MOOSHROOM_SPAWN_EGG;
		if (type == EntityType.GIANT) return XMaterial.ZOMBIE_SPAWN_EGG;
		if (type == EntityType.ARMOR_STAND) return XMaterial.ARMOR_STAND;
		if (type == EntityType.PLAYER) return XMaterial.PLAYER_HEAD;
		if (type == EntityType.ENDER_DRAGON) return XMaterial.DRAGON_HEAD;
		if (type.name().equals("PIG_ZOMBIE") || type.name().equals("ZOMBIFIED_PIGLIN")) return XMaterial.ZOMBIFIED_PIGLIN_SPAWN_EGG;
		if (type.name().equals("ILLUSIONER")) return XMaterial.BLAZE_POWDER;
		return XMaterial.SPONGE;
	}

	public static String clickName(ClickType click) {
		switch (click) {
			case LEFT:
				return Lang.ClickLeft.toString();
			case RIGHT:
				return Lang.ClickRight.toString();
			case SHIFT_LEFT:
				return Lang.ClickShiftLeft.toString();
			case SHIFT_RIGHT:
				return Lang.ClickShiftRight.toString();
			case MIDDLE:
				return Lang.ClickMiddle.toString();
			default:
				return click.name().toLowerCase();
		}
	}

}