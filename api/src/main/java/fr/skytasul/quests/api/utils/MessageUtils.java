package fr.skytasul.quests.api.utils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import net.md_5.bungee.api.ChatColor;

public class MessageUtils {

	private MessageUtils() {}

	private static final Map<Integer, Pattern> REPLACEMENT_PATTERNS = new ConcurrentHashMap<>();
	private static final Pattern RESET_PATTERN = Pattern.compile("§[rR]");

	public static void sendPrefixedMessage(CommandSender sender, String msg, Object... replace) {
		if (StringUtils.isEmpty(msg))
			return;
		sendRawMessage(sender, QuestsPlugin.getPlugin().getPrefix() + msg, false, replace);
	}

	public static void sendUnprefixedMessage(CommandSender sender, String msg, Object... replace) {
		if (StringUtils.isEmpty(msg))
			return;
		sendRawMessage(sender, "§6" + msg, false, replace);
	}

	public static void sendRawMessage(CommandSender sender, String text, boolean playerName, Object... replace) {
		sender.sendMessage(StringUtils.splitByWholeSeparator(finalFormat(sender, text, playerName, replace), "{nl}"));
	}

	public static String finalFormat(CommandSender sender, String text, boolean playerName, Object... replace) {
		if (DependenciesManager.papi.isEnabled() && sender instanceof Player)
			text = QuestsPlaceholders.setPlaceholders((Player) sender, text);
		if (playerName && sender != null)
			text = text.replace("{PLAYER}", sender.getName()).replace("{PREFIX}", QuestsConfiguration.getPrefix());
		text = ChatColor.translateAlternateColorCodes('&', text);
		return format(text, replace);
	}

	public static void sendOffMessage(Player p, String msg, Object... replace) {
		if (msg != null && !msg.isEmpty())
			sendRawMessage(p, Lang.OffText.format(msg, replace), true);
	}

	public static String itemsToFormattedString(String[] items) {
		return itemsToFormattedString(items, "");
	}

	public static String itemsToFormattedString(String[] items, String separator) {
		if (items.length == 0)
			return "";
		if (items.length == 1)
			return items[0];
		if (items.length == 2)
			return items[0] + " " + separator + Lang.And.toString() + " " + ChatColorUtils.getLastColors(null, items[0])
					+ items[1];
		StringBuilder stb = new StringBuilder("§e" + items[0] + ", ");
		for (int i = 1; i < items.length - 1; i++) {
			stb.append(items[i] + ((i == items.length - 2) ? "" : ", "));
		}
		stb.append(" " + Lang.And.toString() + " " + items[items.length - 1]);
		return stb.toString();
	}

	public static @NotNull String format(@NotNull String msg, @NotNull Supplier<Object> @Nullable... replace) {
		if (replace != null && replace.length != 0) {
			for (int i = 0; i < replace.length; i++) {
				Supplier<Object> supplier = replace[i];
				msg = format(msg, i, supplier);
			}
		}
		return msg;
	}

	public static @NotNull String format(@NotNull String msg, @Nullable Object @Nullable... replace) {
		if (replace != null && replace.length != 0) {
			for (int i = 0; i < replace.length; i++) {
				Object replacement = replace[i];
				if (replacement instanceof Supplier) {
					msg = format(msg, i, (Supplier<Object>) replacement);
				} else {
					msg = format(msg, i, () -> replacement);
				}
			}
		}
		return msg;
	}

	public static String format(@NotNull String msg, int i, @NotNull Supplier<Object> replace) {
		Pattern pattern = REPLACEMENT_PATTERNS.computeIfAbsent(i, __ -> Pattern.compile("\\{" + i + "\\}"));
		Matcher matcher = pattern.matcher(msg);
		StringBuilder output = new StringBuilder(msg.length());
		int lastAppend = 0;
		String colors = "";
		String replacement = null;
		while (matcher.find()) {
			String substring = msg.substring(lastAppend, matcher.start());
			colors = ChatColorUtils.getLastColors(colors, substring);
			output.append(substring);
			if (replacement == null)
				replacement = Objects.toString(replace.get());
			Matcher replMatcher = RESET_PATTERN.matcher(replacement);
			output.append(replMatcher.replaceAll("§r" + colors));
			lastAppend = matcher.end();
		}
		output.append(msg, lastAppend, msg.length());
		return output.toString();
	}

}
