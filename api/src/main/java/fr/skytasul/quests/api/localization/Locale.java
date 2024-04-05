package fr.skytasul.quests.api.localization;

import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.utils.ChatColorUtils;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public interface Locale {

	@NotNull
	String getPath();

	@NotNull
	String getValue();

	@NotNull
	MessageType getType();

	void setValue(@NotNull String value);

	default @NotNull String format(@Nullable HasPlaceholders placeholdersHolder) {
		return MessageUtils.format(getValue(),
				placeholdersHolder == null ? null : placeholdersHolder.getPlaceholdersRegistry());
	}

	default @NotNull String format(@NotNull HasPlaceholders @NotNull... placeholdersHolders) {
		return format(PlaceholderRegistry.combine(placeholdersHolders));
	}

	default @NotNull String quickFormat(@NotNull String key1, @Nullable Object value1) {
		// for performance reason: no need to allocate a new placeholder registry with a new placeholder
		String replacement = Objects.toString(value1);
		return getValue()
				.replace("{0}", replacement) // TODO migration 1.0
				.replace("{" + key1 + "}", replacement);
	}

	default void send(@NotNull CommandSender sender) {
		send(sender, (HasPlaceholders) null);
	}

	default void send(@NotNull CommandSender sender, @Nullable HasPlaceholders placeholdersHolder) {
		MessageUtils.sendMessage(sender, getValue(), getType(),
				placeholdersHolder == null ? null : placeholdersHolder.getPlaceholdersRegistry());
	}

	default void send(@NotNull CommandSender sender, @NotNull HasPlaceholders @NotNull... placeholdersHolders) {
		send(sender, PlaceholderRegistry.combine(placeholdersHolders));
	}

	default void quickSend(@NotNull CommandSender sender, @NotNull String key1, @Nullable Object value1) {
		send(sender, PlaceholderRegistry.of(key1, value1));
	}

	public static void loadStrings(@NotNull Locale @NotNull [] locales, @NotNull YamlConfiguration defaultConfig,
			@Nullable YamlConfiguration config) {
		List<String> missing = new ArrayList<>();
		for (Locale l : locales) {
			String value = null;
			if (config != null)
				value = config.getString(l.getPath(), null);
			if (value == null) {
				value = defaultConfig.getString(l.getPath(), null);
				missing.add(l.getPath());
			}
			if (value == null)
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Unavailable string in config for key " + l.getPath());
			l.setValue(ChatColorUtils.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', value == null ? "§cunknown string" : value)));
		}

		if (config != null && !missing.isEmpty()) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("The file is not fully translated! " + missing.size() + " missing translations.");
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.debug("Missing translations: " + String.join(", ", missing));
		}
	}

	public static YamlConfiguration loadLang(@NotNull Plugin plugin, @NotNull Locale @NotNull [] locales,
			@NotNull String loadedLanguage) throws IOException, URISyntaxException {
		long lastMillis = System.currentTimeMillis();

		Utils.walkResources(plugin.getClass(), "/locales", 1, path -> {
			String localeFileName = path.getFileName().toString();
			if (!localeFileName.toLowerCase().endsWith(".yml")) return;

			if (!Files.exists(plugin.getDataFolder().toPath().resolve("locales").resolve(localeFileName))) {
				plugin.saveResource("locales/" + localeFileName, false);
			}
		});

		String language = "locales/" + loadedLanguage + ".yml";
		File file = new File(plugin.getDataFolder(), language);
		InputStream res = plugin.getResource(language);
		boolean created = false;
		if (!file.exists()) {
			plugin.getLogger().warning("Language file " + language + " does not exist. Using default english strings.");
			file.createNewFile();
			res = plugin.getResource("locales/en_US.yml");
			created = true;
		}

		YamlConfiguration def = null;
		if (res != null)
			def = YamlConfiguration.loadConfiguration(new InputStreamReader(res, StandardCharsets.UTF_8));

		YamlConfiguration conf = new YamlConfiguration();
		try {
			// we do NOT use YamlConfiguration#loadConfiguration because it swallows all exceptions
			conf.load(file);
			boolean changes = false;
			if (def != null) { // if it's a local resource
				for (String key : def.getKeys(true)) { // get all keys in resource
					if (!def.isConfigurationSection(key)) { // if not a block
						if (!conf.contains(key)) { // if string does not exist in the file
							conf.set(key, def.get(key)); // copy string
							if (!created)
								QuestsPlugin.getPlugin().getLoggerExpanded()
										.debug("String copied from source file to " + language + ". Key: " + key);
							changes = true;
						}
					}
				}
			}
			if (changes) {
				plugin.getLogger().info("Copied new strings into " + language + " language file.");
				conf.save(file); // if there has been changes before, save the edited file
			}
		} catch (Exception ex) {
			conf = def;
			// the new configuration to load is the default one, or null if unknown language -> will only load
			// default english
			plugin.getLogger().log(Level.SEVERE, "Failed to load language file " + file, ex);
		}
		loadStrings(locales, YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("locales/en_US.yml"), StandardCharsets.UTF_8)), conf);

		plugin.getLogger().info("Loaded language " + loadedLanguage + " (" + (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");
		return conf;
	}

}