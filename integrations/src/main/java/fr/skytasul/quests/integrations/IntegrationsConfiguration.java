package fr.skytasul.quests.integrations;

import org.bukkit.configuration.file.FileConfiguration;

public class IntegrationsConfiguration {

	private final FileConfiguration config;

	private String dSetName = "Quests";
	private String dIcon = "bookshelf";
	private int dMinZoom = 0;

	public IntegrationsConfiguration(FileConfiguration config) {
		this.config = config;
	}

	public void load() {
		if (config.getBoolean("skillAPIoverride")) {

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
