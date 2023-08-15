package fr.skytasul.quests.integrations;

import org.bukkit.configuration.file.FileConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;

public class IntegrationsConfiguration {

	private final FileConfiguration config;

	private String dSetName = "Quests";
	private String dIcon = "bookshelf";
	private int dMinZoom = 0;

	public IntegrationsConfiguration(FileConfiguration config) {
		this.config = config;
	}

	public void load() {
		if (config.getBoolean("skillAPIoverride", false)) {
			config.set("skillAPIoverride", null);
			QuestsPlugin.getPlugin().getLogger().warning("The config option \"skillAPIoverride\" is no longer supported."
					+ " You must change your vanilla XP rewards to specialized SkillAPI XP rewards.");
		}

		dSetName = config.getString("dynmap.markerSetName");
		dIcon = config.getString("dynmap.markerIcon");
		dMinZoom = config.getInt("dynmap.minZoom");
	}

	public String dynmapSetName() {
		return dSetName;
	}

	public String dynmapMarkerIcon() {
		return dIcon;
	}

	public int dynmapMinimumZoom() {
		return dMinZoom;
	}

	public static IntegrationsConfiguration getConfiguration() {
		return IntegrationsLoader.getInstance().getConfig();
	}

}
