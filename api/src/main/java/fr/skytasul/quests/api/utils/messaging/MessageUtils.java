package fr.skytasul.quests.api.utils.messaging;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.ChatColorUtils;

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
		sendRawMessage(sender, message, placeholders, PlaceholdersContext.of(sender, false, type));
	}

	public static void sendRawMessage(@NotNull CommandSender sender, @Nullable String text,
			@Nullable PlaceholderRegistry placeholders, @NotNull PlaceholdersContext context) {
		if (text == null || text.isEmpty())
			return;

		text = finalFormat(text, placeholders, context);
		sender.sendMessage(NEWLINE_PATTERN.split(text));
	}

	public static String finalFormat(@NotNull String text, @Nullable PlaceholderRegistry placeholders,
			@NotNull PlaceholdersContext context) {
		for (MessageProcessor processor : QuestsAPI.getAPI().getMessageProcessors()) {
			placeholders = processor.processPlaceholders(placeholders, context);
			text = processor.processString(text, context);
		}
		return format(text, placeholders, context);
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
		return format(msg, placeholders, PlaceholdersContext.DEFAULT_CONTEXT);
	}

	public static @NotNull String format(@NotNull String msg, @Nullable PlaceholderRegistry placeholders,
			@NotNull PlaceholdersContext context) {
		if (placeholders != null) {
			Matcher matcher = PLACEHOLDER_PATTERN.matcher(Objects.requireNonNull(msg));

			// re-implementation of the "matcher.appendReplacement" system to allow replacement of colors
			StringBuilder output = null;
			int lastAppend = 0;
			String colors = "";
			while (matcher.find()) {
				if (output == null)
					output = new StringBuilder(msg.length());

				String key = matcher.group(1);
				String replacement = placeholders.resolve(key, context);
				String substring = msg.substring(lastAppend, matcher.start());
				colors = ChatColorUtils.getLastColors(colors, substring);
				output.append(substring);
				if (replacement != null)
					output.append(RESET_PATTERN.matcher(replacement).replaceAll("§r" + colors));
				lastAppend = matcher.end();
			}

			if (output != null) {
				output.append(msg, lastAppend, msg.length());
				msg = output.toString();
			}
		}

		return msg;
	}

	public static String getYesNo(boolean bool) {
		return (bool ? Lang.Yes : Lang.No).toString();
	}

	public static String getEnabledDisabled(boolean bool) {
		return (bool ? Lang.Enabled : Lang.Disabled).toString();
	}

}
