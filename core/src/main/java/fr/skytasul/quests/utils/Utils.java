package fr.skytasul.quests.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.util.Consumer;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.QuestsPlaceholders;
import fr.skytasul.quests.utils.nms.NMS;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * A bunch of static methods who can be useful
 * @author SkytAsul
 */
public class Utils{
	
	public static Optional<String> getFilenameExtension(String filename) {
		return Optional.ofNullable(filename).filter(f -> f.contains(".")).map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}
	
	public static void openBook(Player p, ItemStack book){
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);

		ByteBuf buf = Unpooled.buffer(256);
		buf.setByte(0, (byte) 0);
		buf.writerIndex(1);

		NMS.getNMS().sendPacket(p, NMS.getNMS().bookPacket(buf));
		p.getInventory().setItem(slot, old);
	}
	
	
	public static void spawnFirework(Location lc, FireworkMeta meta) {
		if (!QuestsConfiguration.doFireworks() || meta == null) return;
		runOrSync(() -> {
			Consumer<Firework> fwConsumer = fw -> {
				fw.setMetadata("questFinish", new FixedMetadataValue(BeautyQuests.getInstance(), true));
				fw.setFireworkMeta(meta);
			};
			if (NMS.getMCVersion() >= 12) {
				lc.getWorld().spawn(lc, Firework.class, fwConsumer);
				// much better to use the built-in since 1.12 method to do operations on entity
				// before it is sent to the players, as it will not create flickering
			}else {
				fwConsumer.accept(lc.getWorld().spawn(lc, Firework.class));
			}
		});
	}
	
	public static List<String> giveRewards(Player p, List<AbstractReward> rewards) {
		List<String> msg = new ArrayList<>();
		for (AbstractReward rew : rewards) {
			try {
				List<String> messages = rew.give(p);
				if (messages != null) msg.addAll(messages);
			}catch (Exception e) {
				BeautyQuests.logger.severe("Error when giving reward " + rew.getName() + " to " + p.getName());
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	public static String ticksToElapsedTime(int ticks) {
		int i = ticks / 20;
		int j = i / 60;
		i = i % 60;
		return i < 10 ? j + ":0" + i : j + ":" + i;
	}
	
	public static String millisToHumanString(long time) {
		StringBuilder sb = new StringBuilder();
		
		long weeks = time / 604_800_000;
		if (weeks != 0) sb.append(Lang.TimeWeeks.format(weeks));
		time -= weeks * 604_800_000;
		
		long days = time / 86_400_000;
		if (sb.length() != 0) sb.append(' ');
		if (days != 0) sb.append(Lang.TimeDays.format(days));
		time -= days * 86_400_000;
		
		long hours = time / 3_600_000;
		if (sb.length() != 0) sb.append(' ');
		if (hours != 0) sb.append(Lang.TimeHours.format(hours));
		time -= hours * 3_600_000;
		
		long minutes = time / 60_000;
		if (sb.length() != 0) sb.append(' ');
		if (minutes != 0) sb.append(Lang.TimeMinutes.format(minutes));
		time -= minutes * 60_000;
		
		if (sb.length() == 0) sb.append(Lang.TimeLessMinute.toString());
		
		return sb.toString();
	}

	public static String getStringFromItemStack(ItemStack is, String amountColor, boolean showXOne) {
		return ItemUtils.getName(is, true) + ((is.getAmount() > 1 || showXOne) ? "§r" + amountColor + " x" + is.getAmount() : "");
	}
	
	public static String getStringFromNameAndAmount(String name, String amountColor, int remaining, int total, boolean showXOne) {
		return name + ((remaining > 1 || showXOne) ? "§r" + amountColor + " " + Utils.format(QuestsConfiguration.getDescriptionAmountFormat(), remaining, total - remaining, total) : "");
	}
	
	public static void sendMessage(CommandSender sender, String msg, Object... replace){
		if (StringUtils.isEmpty(msg)) return;
		IsendMessage(sender, QuestsConfiguration.getPrefix() + msg, false, replace);
	}
	
	public static void sendMessageWP(CommandSender sender, String msg, Object... replace){
		if (StringUtils.isEmpty(msg)) return;
		IsendMessage(sender, "§6" + msg, false, replace);
	}
	
	public static String finalFormat(CommandSender sender, String text, boolean playerName, Object... replace) {
		if (DependenciesManager.papi.isEnabled() && sender instanceof Player) text = QuestsPlaceholders.setPlaceholders((Player) sender, text);
		if (playerName) text = text.replace("{PLAYER}", sender.getName()).replace("{PREFIX}", QuestsConfiguration.getPrefix());
		return format(text, replace);
	}
	
	public static void IsendMessage(CommandSender sender, String text, boolean playerName, Object... replace) {
		sender.sendMessage(StringUtils.splitByWholeSeparator(finalFormat(sender, text, playerName, replace), "{nl}"));
	}
	
	public static void sendOffMessage(Player p, String msg, Object... replace) {
		if (msg != null && !msg.isEmpty()) IsendMessage(p, Lang.OffText.format(msg, replace), true);
	}
	
	public static String itemsToFormattedString(String[] items){
		return itemsToFormattedString(items, "");
	}
	
	public static String itemsToFormattedString(String[] items, String separator){
		if (items.length == 0) return "";
		if (items.length == 1) return items[0];
		if (items.length == 2) return items[0] + " " + separator + Lang.And.toString() + " " + ChatUtils.getLastColors(null, items[0]) + items[1];
		StringBuilder stb = new StringBuilder("§e" + items[0] + ", ");
		for (int i = 1; i < items.length - 1; i++){
			stb.append(items[i] + ((i == items.length - 2) ? "" : ", "));
		}
		stb.append(" " + Lang.And.toString() + " " + items[items.length - 1]);
		return stb.toString();
	}

	public static String locationToString(Location lc){
		if (lc == null) return null;
		return Lang.teleportation.format(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorld() == null ? Lang.Unknown.toString() : lc.getWorld().getName());
	}
	
	public static Location upLocationForEntity(LivingEntity en, double value) {
		return en.getLocation().add(0, QuestsConfiguration.getHologramsHeight() + NMS.getNMS().entityNameplateHeight(en) + value + (en.getType() != EntityType.PLAYER || Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME) == null ? 0.0 : 0.24), 0);
	}
	
	public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item2.getType() == item1.getType() && item2.getDurability() == item1.getDurability()) {
            try {
				return NMS.getNMS().equalsWithoutNBT(item1.getItemMeta(), item2.getItemMeta());
			}catch (ReflectiveOperationException ex) {
				BeautyQuests.logger.severe("An error occurred while attempting to compare items using NMS");
				ex.printStackTrace();
			}
        }
        return false;
    }
	
	public static void giveItems(Player p, List<ItemStack> items) {
		HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(items.stream().map(ItemStack::clone).toArray(ItemStack[]::new));
		if (!leftover.isEmpty()) {
			leftover.values().forEach(item -> p.getWorld().dropItem(p.getLocation(), item));
			Lang.ITEM_DROPPED.send(p);
		}
	}
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		if (value == null) return null;
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
	
	public static String format(String msg, Supplier<Object>... replace) {
		if (replace != null && replace.length != 0) {
			for (int i = 0; i < replace.length; i++) {
				Supplier<Object> supplier = replace[i];
				msg = format(msg, i, supplier);
			}
		}
		return msg;
	}

	public static String format(String msg, Object... replace){
		if (replace != null && replace.length != 0){
			for (int i = 0; i < replace.length; i++){
				Object replacement = replace[i];
				if (replacement instanceof Supplier) {
					msg = format(msg, i, (Supplier<Object>) replacement);
				}else {
					msg = format(msg, i, () -> replacement);
				}
			}
		}
		return msg;
	}
	
	private static final Map<Integer, Pattern> REPLACEMENT_PATTERNS = new HashMap<>();
	private static final Pattern RESET_PATTERN = Pattern.compile("§[rR]");
	
	public static String format(String msg, int i, Supplier<Object> replace) {
		Pattern pattern = REPLACEMENT_PATTERNS.computeIfAbsent(i, __ -> Pattern.compile("\\{" + i + "\\}"));
		Matcher matcher = pattern.matcher(msg);
		StringBuilder output = new StringBuilder(msg.length());
		int lastAppend = 0;
		String colors = "";
		String replacement = null;
		while (matcher.find()) {
			String substring = msg.substring(lastAppend, matcher.start());
			colors = ChatUtils.getLastColors(colors, substring);
			output.append(substring);
			if (replacement == null) replacement = Objects.toString(replace.get());
			Matcher replMatcher = RESET_PATTERN.matcher(replacement);
			output.append(replMatcher.replaceAll("§r" + colors));
			lastAppend = matcher.end();
		}
		output.append(msg, lastAppend, msg.length());
		return output.toString();
	}
	
	public static String buildFromArray(Object[] array, int start, String insert){
		if (array == null || array.length == 0) return ""; 
		StringBuilder stb = new StringBuilder();
		for (int i = start; i < array.length; i++){
			stb.append(array[i] + ((i == array.length - 1) ? "" : insert));
		}
		return stb.toString();
	}
	
	public static Integer parseInt(CommandSender sender, String arg){
		try{
			return Integer.parseInt(arg);
		}catch (NumberFormatException ex){
			Lang.NUMBER_INVALID.send(sender, arg);
			return null;
		}
	}
	
	public static int parseInt(Object obj){
		if (obj instanceof Number) return ((Number) obj).intValue();
		if (obj instanceof String) return Integer.parseInt((String) obj);
		return 0;
	}
	
	public static long parseLong(Object obj) {
		if (obj instanceof Number) return ((Number) obj).longValue();
		if (obj instanceof String) return Long.parseLong((String) obj);
		return 0;
	}

	public static double parseDouble(Object obj) {
		if (obj instanceof Number) return ((Number) obj).doubleValue();
		if (obj instanceof String) return Double.parseDouble((String) obj);
		return 0;
	}
	
	
	
	public static void runOrSync(Runnable run) {
		if (Bukkit.isPrimaryThread()) {
			run.run();
		}else Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}
	
	public static void runSync(Runnable run){
		Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}
	
	public static void runAsync(Runnable run){
		Bukkit.getScheduler().runTaskAsynchronously(BeautyQuests.getInstance(), run);
	}
	
	public static <T> List<Map<String, Object>> serializeList(Collection<T> objects, Function<T, Map<String, Object>> serialize) {
		List<Map<String, Object>> ls = new ArrayList<>();
		for (T obj : objects){
			ls.add(serialize.apply(obj));
		}
		return ls;
	}
	
	public static <T> List<T> deserializeList(List<Map<String, Object>> serialized, Function<Map<String, Object>, T> deserialize){
		List<T> ls = new ArrayList<>();
		if (serialized != null) {
			for (Map<String, Object> map : serialized) {
				ls.add(deserialize.apply(map));
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
	
	public static void playPluginSound(Player p, String sound, float volume){
		playPluginSound(p, sound, volume, 1);
	}
	
	public static void playPluginSound(Player p, String sound, float volume, float pitch) {
		if (!QuestsConfiguration.playSounds()) return;
		try {
			p.playSound(p.getLocation(), Sound.valueOf(sound), volume, pitch);
		}catch (Exception ex) {
			if (NMS.getMCVersion() > 8) p.playSound(p.getLocation(), sound, volume, pitch);
		}
	}
	
	public static void playPluginSound(Location lc, String sound, float volume){
		if (!QuestsConfiguration.playSounds()) return;
		try {
			lc.getWorld().playSound(lc, Sound.valueOf(sound), volume, 1);
		}catch (Exception ex) {
			if (NMS.getMCVersion() > 8) lc.getWorld().playSound(lc, sound, volume, 1);
		}
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
	
	public static String descriptionLines(Source source, String... elements){
		if (elements.length == 0) return Lang.Unknown.toString();
		if (QuestsConfiguration.splitDescription(source) && (!QuestsConfiguration.inlineAlone() || elements.length > 1)) {
			return QuestsConfiguration.getDescriptionItemPrefix() + buildFromArray(elements, 0, QuestsConfiguration.getDescriptionItemPrefix());
		}
		return itemsToFormattedString(elements, QuestsConfiguration.getItemAmountColor());
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
	
}