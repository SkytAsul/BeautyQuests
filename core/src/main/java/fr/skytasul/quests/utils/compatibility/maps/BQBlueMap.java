package fr.skytasul.quests.utils.compatibility.maps;

import java.io.IOException;
import java.util.function.Consumer;

import org.bukkit.Location;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.POIMarker;

public class BQBlueMap extends AbstractMapIntegration {
	
	private static final String MARKERSET_ID = "beautyquests.markerset";

	private Consumer<BlueMapAPI> enableConsumer;
	
	@Override
	protected void initializeMarkers(Runnable initializeQuests) {
		BlueMapAPI.onEnable(enableConsumer = api -> {
			try {
				MarkerAPI markerAPI = api.getMarkerAPI();
				MarkerSet set = markerAPI.createMarkerSet(MARKERSET_ID);
				set.setLabel(QuestsConfiguration.dynmapSetName());
				set.setToggleable(true);
				set.setDefaultHidden(false);
				markerAPI.save();
				DebugUtils.logMessage("Enabled BlueMap integration.");
				
				initializeQuests.run();
			}catch (IOException e) {
				BeautyQuests.logger.severe("An error occurred while loading BlueMap integration.");
				e.printStackTrace();
				QuestsAPI.unregisterQuestsHandler(this);
			}
		});
	}
	
	@Override
	public void unload() {
		BlueMapAPI.unregisterListener(enableConsumer);
		BlueMapAPI.getInstance().ifPresent(api -> {
			try {
				api.getMarkerAPI().removeMarkerSet(MARKERSET_ID);
			}catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	protected void addMarker(Quest quest, Location lc) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			try {
				MarkerAPI markerAPI = api.getMarkerAPI();
				markerAPI.getMarkerSet(MARKERSET_ID).ifPresent(set -> {
					api.getWorld(lc.getWorld().getUID()).map(BlueMapWorld::getMaps).ifPresent(maps -> {
						int i = 0;
						for (BlueMapMap map : maps) {
							POIMarker marker = set.createPOIMarker("qu_" + quest.getID() + "_" + i++, map, lc.getX(), lc.getY(), lc.getZ());
							marker.setLabel(quest.getName());
							marker.setIcon(QuestsConfiguration.dynmapMarkerIcon(), 0, 0);
						}
						DebugUtils.logMessage("Added " + i + " BlueMap markers for quest " + quest.getID());
					});
				});
				markerAPI.save();
			}catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	@Override
	public void removeMarker(Quest quest) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			try {
				api.getMarkerAPI().getMarkerSet(MARKERSET_ID).ifPresent(set -> {
					Location lc = quest.getOptionValueOrDef(OptionStarterNPC.class).getLocation();
					api.getWorld(lc.getWorld().getUID()).map(BlueMapWorld::getMaps).ifPresent(maps -> {
						for (int i = 0; i < maps.size(); i++) {
							set.removeMarker("qu_" + quest.getID() + "_" + i++);
						}
					});
				});
			}catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
