package fr.skytasul.quests.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import fr.skytasul.quests.utils.ChatUtils;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Utils;

public interface Locale {
	
	String getPath();
	
	String getValue();
	
	void setValue(String value);
	
	default String format(Object... replace) {
		return Utils.format(getValue(), replace);
	}
	
	default String format(Supplier<Object>... replace) {
		return Utils.format(getValue(), replace);
	}
	
	default void send(CommandSender sender, Object... args) {
		Utils.sendMessage(sender, getValue(), args);
	}
	
	default void sendWP(CommandSender p, Object... args) {
		Utils.sendMessageWP(p, getValue(), args);
	}
	
	public static void loadStrings(Locale[] locales, YamlConfiguration defaultConfig, YamlConfiguration config) {
		for (Locale l : locales) {
			String value = config.getString(l.getPath(), null);
			if (value == null) value = defaultConfig.getString(l.getPath(), null);
			if (value == null) DebugUtils.logMessage("Unavailable string in config for key " + l.getPath());
			l.setValue(ChatUtils.translateHexColorCodes(ChatColor.translateAlternateColorCodes('&', value == null ? "§cunknown string" : value)));
		}
	}
	
	public static YamlConfiguration loadLang(Plugin plugin, Locale[] locales, String loadedLanguage, String... languages) throws IOException {
		long lastMillis = System.currentTimeMillis();
		for (String language : languages) {
			File file = new File(plugin.getDataFolder(), "locales/" + language + ".yml");
			if (!file.exists()) plugin.saveResource("locales/" + language + ".yml", false);
		}
		
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