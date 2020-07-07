 package fr.skytasul.quests.utils;

	import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.PlaceholderAPI;
import fr.skytasul.quests.utils.nms.NMS;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * A bunch of static methods who can be useful
 * @author SkytAsul
 */
public class Utils{
	
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
	
	
	public static void spawnFirework(Location lc) {
		if (!QuestsConfiguration.doFireworks()) return;
		runSync(() -> {
			Firework f = (Firework) lc.getWorld().spawnEntity(lc, EntityType.FIREWORK);
			f.setMetadata("questFinish", new FixedMetadataValue(BeautyQuests.getInstance(), true));
			FireworkMeta fm = f.getFireworkMeta();
			fm.addEffect(FireworkEffect.builder().with(Type.BURST).withTrail().withFlicker().withColor(Color.YELLOW, Color.ORANGE).withFade(Color.SILVER).build());
			fm.setPower(1);
			f.setFireworkMeta(fm);
		});
	}
	
	public static List<String> giveRewards(Player p, List<AbstractReward> rewards) {
		List<String> msg = new ArrayList<>();
		for (AbstractReward rew : rewards) {
			try {
				String tmp = rew.give(p);
				if (tmp != null) msg.add(tmp);
			}catch (Throwable e) {
				BeautyQuests.logger.severe("Error when giving reward " + rew.getName() + " to " + p.getName());
				e.printStackTrace();
				continue;
			}
		}
		return msg;
	}

	public static String getStringFromItemStack(ItemStack is, String amountColor, boolean showXOne){
		return getStringFromNameAndAmount(ItemUtils.getName(is, true), amountColor, is.getAmount(), showXOne);
	}
	
	public static String getStringFromNameAndAmount(String name, String amountColor, int amount, boolean showXOne){
		return "§o" + name + ((amount > 1 || showXOne) ? "§r" + amountColor + " x" + amount : "");
	}
	
	public static void sendMessage(CommandSender sender, String msg, Object... replace){
		if (StringUtils.isEmpty(msg)) return;
		IsendMessage(sender, QuestsConfiguration.getPrefix() + format(msg, replace), false);
	}
	
	public static void sendMessageWP(CommandSender sender, String msg, Object... replace){
		if (StringUtils.isEmpty(msg)) return;
		IsendMessage(sender, "§6" + format(msg, replace), false);
	}
	
	public static String finalFormat(CommandSender sender, String text, boolean playerName){
		if (DependenciesManager.papi && sender instanceof Player) text = PlaceholderAPI.setPlaceholders((Player) sender, text);	
		if (playerName) text = text.replace("{PLAYER}", sender.getName());
		return text;
	}
	
	public static void IsendMessage(CommandSender sender, String text, boolean playerName){
		sender.sendMessage(StringUtils.splitByWholeSeparator(finalFormat(sender, text, playerName), "{nl}"));
	}
	
	public static void sendOffMessage(Player p, String msg){
		IsendMessage(p, Lang.OffText.format(msg), true);
	}
	
	public static String itemsToFormattedString(String[] items){
		return itemsToFormattedString(items, "");
	}
	
	public static String itemsToFormattedString(String[] items, String separator){
		if (items.length == 0) return "";
		if (items.length == 1) return items[0];
		if (items.length == 2) return items[0] + " " + separator + Lang.And.toString() + " " + ChatColor.getLastColors(items[0]) + items[1];
		StringBuilder stb = new StringBuilder("§e" + items[0] + ", ");
		for (int i = 1; i < items.length - 1; i++){
			stb.append(items[i] + ((i == items.length - 2) ? "" : ", "));
		}
		stb.append(" " + Lang.And.toString() + " " + items[items.length - 1]);
		return stb.toString();
	}

	public static String locationToString(Location lc){
		return Lang.teleportation.format(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorld().getName());
	}
	
	public static Location upLocationForEntity(LivingEntity en, double value) {
		return en.getLocation().add(0, QuestsConfiguration.getHologramsHeight() + NMS.getNMS().entityNameplateHeight(en) + value + (en.getType() != EntityType.PLAYER || Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME) == null ? 0.0 : 0.24), 0);
	}
	
	public static void removeItems(Inventory inv, ItemStack i){
		if (i.getAmount() <= 0) return;
		ItemStack[] items = inv.getContents();
		for (int slot = 0; slot < items.length; slot++){
			ItemStack item = items[slot];
			if (item == null) continue;
			if (isSimilar(item, i)){
				if (item.getAmount() == i.getAmount()) {
					inv.setItem(slot, new ItemStack(Material.AIR));
                    return;
                }
				if (item.getAmount() > i.getAmount()) {
					item.setAmount(item.getAmount() - i.getAmount());
					return;
				}else if (item.getAmount() < i.getAmount()) {
					i.setAmount(i.getAmount() - item.getAmount());
					inv.setItem(slot, new ItemStack(Material.AIR));
				}
			}
		}
	}

	public static boolean containsItems(Inventory inv, ItemStack i, int amount){
		for(ItemStack item : inv.getContents()) {
			if (item == null) continue;
			if (isSimilar(item, i)){
				if (item.getAmount() == amount) {
					return true;
                }
				if (item.getAmount() > amount) {
					return true;
				}else if (item.getAmount() < amount) {
					amount -= item.getAmount();
				}
			}
		}
		return false;
	}
	
	public static boolean isSimilar(ItemStack item1, ItemStack item2) {
        if (item2.getType() == item1.getType() && item2.getDurability() == item1.getDurability()) {
            ItemMeta item1Meta = item1.getItemMeta();
            ItemMeta item2Meta = item2.getItemMeta();

			try {
				return NMS.getNMS().equalsWithoutNBT(item1Meta, item2Meta);
			}catch (ReflectiveOperationException ex) {
				ex.printStackTrace();
			}

			/*if (item1Meta.hasDisplayName() != item2Meta.hasDisplayName()) return false;
			if (item1Meta.hasDisplayName()) {
			    if (!item1Meta.getDisplayName().equals(item2Meta.getDisplayName())) return false;
			}
			
			if (item1Meta.hasLore() != item2Meta.hasLore()) return false;
			if (item1Meta.hasLore()) {
			    if (item1Meta.getLore().size() != item2Meta.getLore().size()) return false;
			    for (int index = 0; index < item1Meta.getLore().size(); index++) {
			        if (!item1Meta.getLore().get(index).equals(item2Meta.getLore().get(index))) return false;
			    }
			}
			
			if (item1Meta.hasEnchants() != item2Meta.hasEnchants()) return false;
			if (item1Meta.hasEnchants()) {
			    if (item1Meta.getEnchants().size() != item2Meta.getEnchants().size()) return false;
			    for (Entry<Enchantment, Integer> enchantInfo : item1Meta.getEnchants().entrySet()) {
			        if (item1Meta.getEnchantLevel(enchantInfo.getKey()) != item2Meta.getEnchantLevel(enchantInfo.getKey())) return false;
			    }
			}
			if (item1Meta.getItemFlags().size() != item2Meta.getItemFlags().size()) return false;
			for (ItemFlag flag : item1Meta.getItemFlags()) {
			    if (!item2Meta.hasItemFlag(flag)) return false;
			}*/

            return true;
        }
        return false;
    }
	
	public static void giveItem(Player p, ItemStack is){
		if (p.getInventory().firstEmpty() == -1){
			p.getWorld().dropItem(p.getLocation(), is);
			Lang.ITEM_DROPPED.send(p);
		}else {
			p.getInventory().addItem(is);
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
		if (value == null) return Collections.EMPTY_LIST;
		List<T> list = new ArrayList<>();
		for (Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				list.add(entry.getKey());
			}
		}
		return list;
	}

	public static String format(String msg, Object... replace){
		if (replace != null && replace.length != 0){
			for (int i = 0; i < replace.length; i++){
				msg = format(msg, i, (replace[i] != null) ? replace[i].toString() : "null");
			}
		}
		return msg;
	}
	
	public static String format(String msg, int i, String replace){
		String tmp = new String(msg);
		tmp = tmp.replace("{" + i + "}", replace);
		return tmp;
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

	public static String removeColors(String str){
		int i;
		while ((i = str.indexOf("§")) != -1){
			str = str.substring(0, i) + str.substring(i + 2);
		}
		return str;
	}
	
	/**
	 * Breaks a raw string up into a series of lines. Words are wrapped using
	 * spaces as decimeters and the newline character is respected.
	 *
	 * @param rawString The raw string to break.
	 * @param lineLength The length of a line of text.
	 * @return An array of word-wrapped lines.
	 */
	public static List<String> wordWrap(String rawString, int lineLength) {
		// A null string is a single line
		if (rawString == null) {
			return Arrays.asList("");
		}
		
		rawString = rawString.replace("{nl}", "\n");
		
		// A string shorter than the lineWidth is a single line
		if (rawString.length() <= lineLength && !rawString.contains("\n")) {
			return Arrays.asList(rawString);
		}
		
		char[] rawChars = (rawString + ' ').toCharArray(); // add a trailing space to trigger pagination
		StringBuilder word = new StringBuilder();
		StringBuilder line = new StringBuilder();
		List<String> lines = new LinkedList<String>();
		
		for (int i = 0; i < rawChars.length; i++) {
			char c = rawChars[i];
			
			// skip chat color modifiers
			if (c == ChatColor.COLOR_CHAR) {
				word.append(ChatColor.getByChar(rawChars[i + 1]));
				i++; // Eat the next character as we have already processed it
				continue;
			}
			
			if (c == ' ' || c == '\n') {
				if (line.length() == 0 && word.length() > lineLength) { // special case: extremely long word begins a line
					for (String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
						lines.add(partialWord);
					}
				}else if (line.length() + 1 + word.length() == lineLength) { // Line exactly the correct length...newline
					if (line.length() > 0) {
						line.append(' ');
					}
					line.append(word);
					lines.add(line.toString());
					line = new StringBuilder();
				}else if (line.length() + 1 + word.length() > lineLength) { // Line too long...break the line
					for (String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
						lines.add(line.toString());
						line = new StringBuilder(partialWord);
					}
				}else {
					if (line.length() > 0) {
						line.append(' ');
					}
					line.append(word);
				}
				word = new StringBuilder();
				
				if (c == '\n') { // Newline forces the line to flush
					lines.add(line.toString());
					line = new StringBuilder();
				}
			}else {
				word.append(c);
			}
		}
		
		if (line.length() > 0) { // Only add the last line if there is anything to add
			lines.add(line.toString());
		}
		
		// Iterate over the wrapped lines, applying the last color from one line to the beginning of the next
		/*if (lines.get(0).length() == 0 || lines.get(0).charAt(0) != ChatColor.COLOR_CHAR) {
			lines.set(0, ChatColor.WHITE + lines.get(0));
		}*/
		for (int i = 1; i < lines.size(); i++) {
			final String pLine = lines.get(i - 1);
			final String subLine = lines.get(i);
			
			//String color = ChatColor.getByChar(pLine.charAt(pLine.lastIndexOf(ChatColor.COLOR_CHAR) + 1));
			String color = ChatColor.getLastColors(pLine);
			if (subLine.length() == 0 || subLine.charAt(0) != ChatColor.COLOR_CHAR) {
				lines.set(i, color + subLine);
			}
		}
		
		return lines;
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
		for (Map<String, Object> map : serialized){
			ls.add(deserialize.apply(map));
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
	
	private static SimpleDateFormat cachedFormat = new SimpleDateFormat("yyyyMMddHHmmss");;
	public static DateFormat getDateFormat(){
		return cachedFormat;
	}
	
	public static void playPluginSound(Player p, String sound, float volume){
		if (!QuestsConfiguration.playSounds()) return;
		try {
			p.playSound(p.getLocation(), Sound.valueOf(sound), volume, 1);
		}catch (Throwable ex){
			if (NMS.getMCVersion() > 8) p.playSound(p.getLocation(), sound, volume, 1);
		}
	}
	
	public static void playPluginSound(Location lc, String sound, float volume){
		if (!QuestsConfiguration.playSounds()) return;
		try {
			lc.getWorld().playSound(lc, Sound.valueOf(sound), volume, 1);
		}catch (Throwable ex){
			if (NMS.getMCVersion() > 8) lc.getWorld().playSound(lc, sound, volume, 1);
		}
	}
	
	/*public static List<String> serializeAccountsList(List<PlayerAccount> from){
		List<String> to = new ArrayList<>();
		for (PlayerAccount acc : from){
			to.add(acc.getIndex());
		}
		return to;
	}
	
	public static void deserializeAccountsList(List<PlayerAccount> to, List<String> from){
		for (String id : from){
			PlayerAccount acc = PlayersManager.manager.getByIndex(id);
			if (acc != null) to.add(acc);
		}
	}
	
	public static <T, R> void deserializeAccountsMap(Map<String, T> from, Map<PlayerAccount, R> to, Function<T, R> fun){
		for (Entry<String, T> en : from.entrySet()){
			PlayerAccount acc = PlayersManager.manager.getByIndex(en.getKey());
			if (acc == null) continue;
			to.put(acc, fun.apply(en.getValue()));
		}
	}*/
	
	public static String descriptionLines(Source source, String... elements){
		if (elements.length == 0) return Lang.Unknown.toString();
		if (QuestsConfiguration.splitDescription(source) && (!QuestsConfiguration.inlineAlone() || elements.length > 1)) {
			return QuestsConfiguration.getDescriptionItemPrefix() + buildFromArray(elements, 0, QuestsConfiguration.getDescriptionItemPrefix());
		}
		return itemsToFormattedString(elements, QuestsConfiguration.getItemAmountColor());
	}
	
}