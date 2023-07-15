package fr.skytasul.quests.api.utils.messaging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.api.utils.SplittableDescriptionConfiguration;
import net.md_5.bungee.api.ChatColor;

public class MessageUtils {

	private MessageUtils() {}

	private static final Pattern RESET_PATTERN = Pattern.compile("§[rR]");
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_-]+)\\}");
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("(?:\\n|\\\\n|\\{nl\\})");

	public static void sendMessage(@NotNull CommandSender sender, @Nullable String message, @NotNull MessageType type) {
		sendMessage(sender, message, type, null);
	}

	public static void sendMessage(@NotNull CommandSender sender, @Nullable String message, @NotNull MessageType type,
			@Nullable PlaceholderRegistry placeholders) {
		if (message == null || message.isEmpty())
			return;

		sendRawMessage(sender, type.process(message), false, placeholders);
	}

	public static void sendRawMessage(@NotNull CommandSender sender, @Nullable String text, boolean playerName,
			@Nullable PlaceholderRegistry placeholders) {
		if (text == null || text.isEmpty())
			return;

		text = finalFormat(sender, text, playerName, placeholders);
		sender.sendMessage(NEWLINE_PATTERN.split(text));
	}

	public static String finalFormat(@Nullable CommandSender sender, @NotNull String text, boolean playerName,
			@Nullable PlaceholderRegistry placeholders) {
		if (DependenciesManager.papi.isEnabled() && sender instanceof Player)
			text = QuestsPlaceholders.setPlaceholders((Player) sender, text);
		if (playerName && sender != null)
			text = text.replace("{PLAYER}", sender.getName()).replace("{PREFIX}", QuestsConfiguration.getPrefix());
		text = ChatColor.translateAlternateColorCodes('&', text);
		return format(text, placeholders);
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

	public static @NotNull String format(@NotNull String msg, @Nullable PlaceholderRegistry placeholders) {
		if (placeholders != null) {
			Matcher matcher = PLACEHOLDER_PATTERN.matcher(msg);

			// re-implementation of the "matcher.appendReplacement" system to allow replacement of colors
			StringBuilder output = null;
			int lastAppend = 0;
			String colors = "";
			while (matcher.find()) {
				if (output == null)
					output = new StringBuilder(msg.length());

				String key = matcher.group(1);
				String replacement = placeholders.resolve(key);
				String substring = msg.substring(lastAppend, matcher.start());
				colors = ChatColorUtils.getLastColors(colors, substring);
				output.append(substring);
				Matcher replMatcher = RESET_PATTERN.matcher(replacement);
				output.append(replMatcher.replaceAll("§r" + colors));
				lastAppend = matcher.end();
			}

			if (output != null) {
				output.append(msg, lastAppend, msg.length());
				msg = output.toString();
			}
		}

		return msg;
	}

	public static String formatDescription(DescriptionSource source, SplittableDescriptionConfiguration configuration,
			String... elements) {
		if (elements.length == 0)
			return Lang.Unknown.toString();
		if (elements.length == 1 && configuration.isAloneSplitAmountShown(source))
			return MessageUtils.itemsToFormattedString(elements, configuration.getItemAmountColor());
		return String.join(configuration.getSplitPrefix(), elements);
	}

	public static String getYesNo(boolean bool) {
		return (bool ? Lang.Yes : Lang.No).toString();
	}

	public static String getEnabledDisabled(boolean bool) {
		return (bool ? Lang.Enabled : Lang.Disabled).toString();
	}

}
