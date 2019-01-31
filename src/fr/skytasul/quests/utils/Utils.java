 package fr.skytasul.quests.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.PlaceholderAPI;
import fr.skytasul.quests.utils.types.Dialog;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.citizensnpcs.api.npc.NPC;

public class Utils{
	
	public static void openBook(Player p, ItemStack book){
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		p.getInventory().setItem(slot, book);

		ByteBuf buf = Unpooled.buffer(256);
		buf.setByte(0, (byte) 0);
		buf.writerIndex(1);

		BeautyQuests.nms.sendPacket(p, BeautyQuests.nms.bookPacket(buf));
		p.getInventory().setItem(slot, old);
	}
	
	
	public static void spawnFirework(Location lc) {
		if (!QuestsConfiguration.doFireworks()) return;
		BukkitRunnable run = new BukkitRunnable() {
			public void run(){
				Firework f = (Firework) lc.getWorld().spawnEntity(lc, EntityType.FIREWORK);
				f.setMetadata("questFinish", new FixedMetadataValue(BeautyQuests.getInstance(), true));
				FireworkMeta fm = f.getFireworkMeta();
				fm.addEffect(FireworkEffect.builder().with(Type.BURST).withTrail().withFlicker().withColor(Color.YELLOW, Color.ORANGE).withFade(Color.SILVER).build());
				fm.setPower(1);
				f.setFireworkMeta(fm);
			}
		};
		run.runTask(BeautyQuests.getInstance());
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

	public static String getStringFromItemStack(ItemStack is, String amountColor){
		return getStringFromNameAndAmount(ItemUtils.getName(is, true), amountColor, is.getAmount());
	}

	public static String[] getStringArrayFromItemStackArray(ItemStack[] items, String amountColor, String itemColor){
		String[] str = new String[items.length];
		for (int i = 0; i < items.length; i++){
			str[i] = itemColor + getStringFromItemStack(items[i], amountColor);
		}
		return str;
	}
	
	public static String getStringFromNameAndAmount(String name, String amountColor, int amount){
		return "§o" + name + ((amount > 1) ? "§r" + amountColor + " x" + amount : "");
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
		IsendMessage(p, Lang.NpcText.format(npc.getName(), msg, index, max), true);
	}
	
	public static void sendSelfMessage(Player p, String msg, int index, int max, Object... replace){
		if (replace.length != 0){
			for (int i = 0; i < replace.length; i++){
				msg = format(msg, i, replace[i].toString());
			}
		}
		IsendMessage(p, Lang.SelfText.format(p.getName(), msg, index, max), true);
	}
	
	public static void IsendMessage(CommandSender sender, String text, boolean playerName){
		if (Dependencies.papi && sender instanceof Player) text = PlaceholderAPI.setPlaceholders((Player) sender, text);	
		if (playerName) text = text.replace("{PLAYER}", sender.getName());
		sender.sendMessage(text);
	}
	
	public static boolean startDialog(Player p, Dialog di){
		if (!di.messages.isEmpty()){
			di.send(p, 0);
			return true;
		}
		return false;
	}
	
	public static void sendOffMessage(Player p, String msg){
		IsendMessage(p, Lang.OffText.format(msg), true);
	}
	
	public static String[] arrayFromEnumList(List<? extends Enum<?>> list){
		String[] array = new String[list.size()];
		for(int i = 0; i < list.size(); i++){
			array[i] = ((Enum<?>) list.get(i)).name();
		}
		return array;
	}
	
	public static List<String> stringListFromEnumList(List<? extends Enum<?>> list){
		List<String> tmp = new ArrayList<>();
		for (Enum<?> value : list){
			tmp.add(value.name());
		}
		return tmp;
	}
	
	public static String getStringFromStringArray(String[] args){
		if (args == null || args.length == 0) return ""; 
		StringBuilder stb = new StringBuilder();
		for (int i = 0; i < args.length; i++){
			stb.append(args[i] + (i+1 == args.length ? "" : " "));
		}
		return stb.toString();
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
	
	public static String itemsToString(String[] items, String separator){
		String string = "";
		for (int i = 0; i < items.length; i++) {
			string = string + (i != 0 ? separator + items[i] : items[i]);
		}
		return string;
	}

	public static String locationToString(Location lc, boolean world){
		return "X: " + lc.getBlockX() + " | Y: " + lc.getBlockY() + " | Z:" + lc.getBlockZ() + (world ? " | World: " + lc.getWorld().getName() : "");
	}
	
	public static Location upLocationForEntity(LivingEntity en, double value) {
		return en.getEyeLocation().add(0, QuestsConfiguration.getHologramsHeight() + value + (en.getType() != EntityType.PLAYER || Bukkit.getScoreboardManager().getMainScoreboard().getObjective(DisplaySlot.BELOW_NAME) == null ? 0.0 : 0.24), 0);
	}
	
	public static void removeItems(Inventory inv, ItemStack i){
		for(ItemStack item : inv.getContents()) {
			if (i.getAmount() <= 0) return;
			if (item == null) continue;
			if (item.isSimilar(i)){
				if (item.getAmount() == i.getAmount()) {
                    int first = inv.first(i);
                    if (first != -1) inv.setItem(first, new ItemStack(Material.AIR));
                    return;
                } else {
                    if(item.getAmount() > i.getAmount()){
                        item.setAmount(item.getAmount() - i.getAmount());
                        return;
                    }else if(item.getAmount() < i.getAmount()){
                        i.setAmount(i.getAmount() - item.getAmount());
                        int first = inv.first(item);
                        if (first == -1){
                        	BeautyQuests.getInstance().getLogger().warning("Unexpected error -1 on removeItems in fr.skytasul.quests.utils.Utils. Please report this to SkytAsul on SpigotMC.");
                        	continue;
                        }
                        inv.setItem(first, new ItemStack(Material.AIR));
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
		if (replace.length != 0){
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
		if (string == null || string.isEmpty()) return null; 
		List<String> ls = new ArrayList<>();
		if (string.length() <= minSize){
			ls.add(string);
			return ls;
		}
		
		for (String str : StringUtils.splitByWholeSeparator(string, "\\n")) {
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
	
	/*@Deprecated
	public static List<AbstractReward> convertFromOldRewards(Map<String, Object> map){
		List<AbstractReward> rewards = new ArrayList<>();
		
		List<ItemStack> ls = new ArrayList<>();
		for (Map<String, Object> m : (List<Map<String, Object>>) map.get("items")){
			ls.add(ItemStack.deserialize(m));
		}
		if (ls.size() > 0) rewards.add(new ItemReward(ls));
		if ((int) map.get("xp") > 0) rewards.add(new XPReward((int) map.get("xp")));
		if (map.containsKey("perm")) rewards.add(new PermissionReward((String) map.get("perm")));
		if (map.containsKey("money")) rewards.add(new MoneyReward((int) map.get("money")));
		
		return rewards;
	}
	
	@Deprecated
	public static List<AbstractReward> convertFromOldEnding(Map<String, Object> map){
		List<AbstractReward> rewards = map.containsKey("rew") ? convertFromOldRewards((Map<String, Object>) map.get("rew")) : new ArrayList<>();

		if (map.containsKey("tp")) rewards.add(new TeleportationReward(Location.deserialize((Map<String, Object>) map.get("tp"))));
		if (map.containsKey("text")) rewards.add(new MessageReward((String) map.get("text")));
		if (map.containsKey("cmd")){
			Map<String, Object> cmd = (Map<String, Object>) map.get("cmd");
			rewards.add(new CommandReward((String) cmd.get("command"), (boolean) cmd.get("console")));
		}
		
		return rewards;
	}*/
	
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
	
	public static void deserializeAccountsList(List<PlayerAccount> list, List<String> strings){
		for (String id : strings){
			PlayerAccount acc = PlayersManager.getByIndex(id);
			if (acc != null) list.add(acc);
		}
	}
	
}