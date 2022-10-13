package fr.skytasul.quests.utils.compatibility.maps;

import java.util.function.Consumer;

import org.bukkit.Location;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.DebugUtils;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

public class BQBlueMap extends AbstractMapIntegration {
	
	private static final String MARKERSET_ID = "beautyquests.markerset";

	private Consumer<BlueMapAPI> enableConsumer;
	
	@Override
	protected void initializeMarkers(Runnable initializeQuests) {
		BlueMapAPI.onEnable(enableConsumer = api -> {
			DebugUtils.logMessage("Enabled BlueMap integration.");
			initializeQuests.run();
		});
	}
	
	@Override
	public void unload() {
		BlueMapAPI.unregisterListener(enableConsumer);
		BlueMapAPI.getInstance().ifPresent(api -> {
			for (BlueMapMap map : api.getMaps()) {
				map.getMarkerSets().remove(MARKERSET_ID);
			}
		});
	}
	
	@Override
	protected void addMarker(Quest quest, Location lc) {
		BlueMapAPI.getInstance()
				.flatMap(api -> api.getWorld(lc.getWorld()))
				.map(BlueMapWorld::getMaps)
				.ifPresent(maps -> {
					int i = 0;
					for (BlueMapMap map : maps) {
						MarkerSet set = map.getMarkerSets().computeIfAbsent(MARKERSET_ID, this::createMarkerSet);
						set.getMarkers().put("qu_" + quest.getID(), POIMarker.builder()
								.label(quest.getName())
								.icon(QuestsConfiguration.dynmapMarkerIcon(), 0, 0)
								.build()
						);
					}
					DebugUtils.logMessage("Added " + i + " BlueMap markers for quest " + quest.getID());
				});
	}
	
	@Override
	public void removeMarker(Quest quest) {
		BlueMapAPI.getInstance()
				.map(BlueMapAPI::getMaps)
				.ifPresent(maps -> {
					for (BlueMapMap map : maps) {
						MarkerSet set = map.getMarkerSets().get(MARKERSET_ID);
						if (set != null) {
							set.getMarkers().remove("qu_" + quest.getID());
						}
					}
				});
	}

	private MarkerSet createMarkerSet(String id) {
		return MarkerSet.builder()
				.label(QuestsConfiguration.dynmapSetName())
				.toggleable(true)
				.defaultHidden(false)
				.build();
	}

}
