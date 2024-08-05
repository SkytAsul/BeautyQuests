package fr.skytasul.quests.integrations.maps;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import fr.skytasul.quests.api.AbstractMapIntegration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.integrations.IntegrationsConfiguration;
import org.bukkit.Location;
import java.util.function.Consumer;

public class BQBlueMap extends AbstractMapIntegration {

	private static final String MARKERSET_ID = "beautyquests.markerset";

	private Consumer<BlueMapAPI> enableConsumer;

	@Override
	public boolean isEnabled() {
		return IntegrationsConfiguration.getConfiguration().dynmapMarkerIcon() != null
				&& !IntegrationsConfiguration.getConfiguration().dynmapMarkerIcon().isEmpty();
	}

	@Override
	protected void initializeMarkers(Runnable initializeQuests) {
		BlueMapAPI.onEnable(enableConsumer = api -> {
			try {
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Enabled BlueMap integration.");
				initializeQuests.run();
			}catch (Exception e) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while loading BlueMap integration.", e);
				QuestsAPI.getAPI().unregisterQuestsHandler(this);
			}
		});
	}

	@Override
	public void unload() {
		BlueMapAPI.unregisterListener(enableConsumer);
		BlueMapAPI.getInstance().ifPresent(api -> {
			api.getMaps().forEach(map -> map.getMarkerSets().remove(MARKERSET_ID));
		});
	}

	@Override
	protected void addMarker(Quest quest, Location lc) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			api.getWorld(lc.getWorld()).map(BlueMapWorld::getMaps).ifPresent(maps -> {
				for (BlueMapMap map : maps) {
					MarkerSet set = map.getMarkerSets().computeIfAbsent(MARKERSET_ID, __ -> MarkerSet.builder()
							.label(IntegrationsConfiguration.getConfiguration().dynmapSetName())
							.defaultHidden(false)
							.toggleable(true)
							.build());

					POIMarker marker = POIMarker.builder()
							.label(quest.getName())
							.icon(IntegrationsConfiguration.getConfiguration().dynmapMarkerIcon(), 0, 0)
							.position(lc.getX(), lc.getY(), lc.getZ())
							.build();
					set.getMarkers().put("qu_" + quest.getId(), marker);
				}
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.debug("Added " + maps.size() + " BlueMap markers for quest " + quest.getId());
			});
		});
	}

	@Override
	public void removeMarker(Quest quest) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			Location lc = quest.getStarterNpc().getLocation();
			api.getWorld(lc.getWorld()).map(BlueMapWorld::getMaps).ifPresent(maps -> {
				for (BlueMapMap map : maps) {
					MarkerSet set = map.getMarkerSets().get(MARKERSET_ID);
					if (set != null)
						set.getMarkers().remove("qu_" + quest.getId());
				}
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.debug("Deleted " + maps.size() + " BlueMap markers for quest " + quest.getId());
			});
		});
	}

}
