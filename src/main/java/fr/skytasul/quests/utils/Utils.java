 package fr.skytasul.quests.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scoreboard.DisplaySlot;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.PlaceholderAPI;
import fr.skytasul.quests.utils.nms.NMS;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.citizensnpcs.api.npc.NPC;

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
	
	public static void sendNPCMessage(Player p, String msg, NPC npc, int index, int max, Object... replace){
		if (replace.length != 0){
			for (int i = 0; i < replace.length; i++){
				msg = format(msg, i, replace[i].toString());
			}
		}
		String npcName = npc.getName();
		IsendMessage(p, Lang.NpcText.format(npcName, msg, index, max), true);
	}
	
	public static void sendSelfMessage(Player p, String msg, int index, int max, Object... replace){
		if (replace.length != 0){
			for (int i = 0; i < replace.length; i++){
				msg = format(msg, i, replace[i].toString());
			}
		}
		IsendMessage(p, Lang.SelfText.format(p.getName(), msg, index, max), true);
	}
	
	public static String finalFormat(CommandSender sender, String text, boolean playerName){
		if (Dependencies.papi && sender instanceof Player) text = PlaceholderAPI.setPlaceholders((Player) sender, text);	
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

	public static String locationToString(Location lc, boolean world){
		return "X: " + lc.getBlockX() + " | Y: " + lc.getBlockY() + " | Z:" + lc.getBlockZ() + (world ? " | World: " + lc.getWorld().getName() : "");
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
			if (item.isSimilar(i)){
				if (item.getAmount() == i.getAmount()) {
					inv.setItem(slot, new ItemStack(Material.AIR));
                    return;
                } else {
                    if(item.getAmount() > i.getAmount()){
                        item.setAmount(item.getAmount() - i.getAmount());
                        return;
                    }else if(item.getAmount() < i.getAmount()){
                        i.setAmount(i.getAmount() - item.getAmount());
                        inv.setItem(slot, new ItemStack(Material.AIR));
                    }
                }
			}
		}
	}

	public static boolean containsItems(Inventory inv, ItemStack i, int amount){
		for(ItemStack item : inv.getContents()) {
			if (item == null) continue;
			if (item.isSimilar(i)){
				if (item.getAmount() == amount) {
					return true;
                } else {
                    if(item.getAmount() > amount){
                    	return true;
                    }else if(item.getAmount() < amount){
                        amount -= item.getAmount();
                    }
                }
			}
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
	
	public static OfflinePlayer getOfflinePlayer(String name){
		Validate.notNull(name);
		Player pp = Bukkit.getPlayer(name);
		if (pp == null){
			OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
			if (ops == null) return null;
			for (OfflinePlayer tmp : ops){
				if (name.equals(tmp.getName())) return tmp;
			}
			return null;
		}else return pp;
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
	
	public static String removeColors(String str){
		int i;
		while ((i = str.indexOf("§")) != -1){
			str = str.substring(0, i) + str.substring(i + 2);
		}
		return str;
	}
	
	public static List<String> splitOnSpace(String string, int minSize){
		if (string == null) return null; 
		List<String> ls = new ArrayList<>();
		if (string.isEmpty()){
			ls.add("");
			return ls;
		}
		
		minSize--;
		for (String str : StringUtils.splitByWholeSeparator(string, ("{nl}"))) {
			int lastI = 0;
			int ic = 0;
			for (int i = 0; i < str.length(); i++){
				String color = "";
				if (!ls.isEmpty()) color = ChatColor.getLastColors(ls.get(ls.size() - 1));
				if (ic >= minSize){
					if (str.charAt(i) == ' '){
						ls.add(color + str.substring(lastI, i));
						ic = 0;
						lastI = i + 1;
					}else if (i + 1 == str.length()){
						ls.add(color + str.substring(lastI, i + 1));
					}
				}else if (str.length() - lastI <= minSize){
					ls.add(color + str.substring(lastI, str.length()));
					break;
				}
				ic++;
			}
		}
		
		return ls;
	}
	
	public static void runSync(Runnable run){
		Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), run);
	}
	
	public static void runAsync(Runnable run){
		Bukkit.getScheduler().runTaskAsynchronously(BeautyQuests.getInstance(), run);
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
			p.playSound(p.getLocation(), sound, volume, 1);
		}
	}
	
	public static void playPluginSound(Location lc, String sound, float volume){
		if (!QuestsConfiguration.playSounds()) return;
		try {
			lc.getWorld().playSound(lc, Sound.valueOf(sound), volume, 1);
		}catch (Throwable ex){
			lc.getWorld().playSound(lc, sound, volume, 1);
		}
	}
	
	public static void deserializeAccountsList(List<PlayerAccount> to, List<String> from){
		for (String id : from){
			PlayerAccount acc = PlayersManager.getByIndex(id);
			if (acc != null) to.add(acc);
		}
	}
	
	public static <T, R> void deserializeAccountsMap(Map<String, T> from, Map<PlayerAccount, R> to, Function<T, R> fun){
		for (Entry<String, T> en : from.entrySet()){
			PlayerAccount acc = PlayersManager.getByIndex(en.getKey());
			if (acc == null) continue;
			to.put(acc, fun.apply(en.getValue()));
		}
	}
	
	public static String descriptionLines(Source source, String... elements){
		if (elements.length == 0) return Lang.Unknown.toString();
		if (QuestsConfiguration.splitDescription(source) && elements.length > 1){
			return QuestsConfiguration.getDescriptionItemPrefix() + buildFromArray(elements, 0, QuestsConfiguration.getDescriptionItemPrefix());
		}else {
			return itemsToFormattedString(elements, QuestsConfiguration.getItemAmountColor());
		}
	}
	
}