package fr.skytasul.quests.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Supplier;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.utils.ChatUtils;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Utils;

public interface Locale {
	
	@NotNull
	String getPath();
	
	@NotNull
	String getValue();
	
	void setValue(@NotNull String value);
	
	default @NotNull String format(@Nullable Object @Nullable... replace) {
		return Utils.format(getValue(), replace);
	}
	
	default @NotNull String format(@NotNull Supplier<Object> @Nullable... replace) {
		return Utils.format(getValue(), replace);
	}
	
	default void send(@NotNull CommandSender sender, @Nullable Object @Nullable... args) {
		Utils.sendMessage(sender, getValue(), args);
	}
	
	default void sendWP(@NotNull CommandSender p, @Nullable Object @Nullable... args) {
		Utils.sendMessageWP(p, getValue(), args);
	}
	
	public static void loadStrings(@NotNull Locale @NotNull [] locales, @NotNull YamlConfiguration defaultConfig,
			@NotNull YamlConfiguration config) {
		for (Locale l : locales) {
			String value = config.getString(l.getPath(), null);
			if (value == null) value = defaultConfig.getString(l.getPath(), null);
			if (value == null) DebugUtils.logMessage("Unavailable string in config for key " + l.getPath());
			l.setValue(ChatUtils.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', value == null ? "§cunknown string" : value)));
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
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
		boolean changes = false;
		if (res != null) { // if it's a local resource
			YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(res, StandardCharsets.UTF_8));
			for (String key : def.getKeys(true)) { // get all keys in resource
				if (!def.isConfigurationSection(key)) { // if not a block
					if (!conf.contains(key)) { // if string does not exist in the file
						conf.set(key, def.get(key)); // copy string
						if (!created) DebugUtils.logMessage("String copied from source file to " + language + ". Key: " + key);
						changes = true;
					}
				}
			}
		}
		loadStrings(locales, YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("locales/en_US.yml"), StandardCharsets.UTF_8)), conf);
		
		if (changes) {
			plugin.getLogger().info("Copied new strings into " + language + " language file.");
			conf.save(file); // if there has been changes before, save the edited file
		}
		
		plugin.getLogger().info("Loaded language " + loadedLanguage + " (" + (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");
		return conf;
	}
	
}