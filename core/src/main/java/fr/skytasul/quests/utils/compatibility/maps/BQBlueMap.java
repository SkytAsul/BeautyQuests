package fr.skytasul.quests.utils.compatibility.maps;

import java.util.function.Consumer;
import org.bukkit.Location;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.options.OptionStarterNPC;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

public class BQBlueMap extends AbstractMapIntegration {
	
	private static final String MARKERSET_ID = "beautyquests.markerset";

	private Consumer<BlueMapAPI> enableConsumer;
	private MarkerSet set;
	
	@Override
	public boolean isEnabled() {
		return QuestsConfigurationImplementation.dynmapMarkerIcon() != null && !QuestsConfigurationImplementation.dynmapMarkerIcon().isEmpty();
	}

	@Override
	protected void initializeMarkers(Runnable initializeQuests) {
		BlueMapAPI.onEnable(enableConsumer = api -> {
			try {
				set = MarkerSet.builder()
						.label(QuestsConfigurationImplementation.dynmapSetName())
						.defaultHidden(false)
						.toggleable(true)
						.build();
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
				int i = 0;
				for (BlueMapMap map : maps) {
					POIMarker marker = POIMarker.toBuilder()
							.label(quest.getName())
							.icon(QuestsConfigurationImplementation.dynmapMarkerIcon(), 0, 0)
							.position(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ())
							.build();
					set.getMarkers().put("qu_" + quest.getId() + "_" + i++, marker);
					map.getMarkerSets().putIfAbsent(MARKERSET_ID, set);
				}
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Added " + i + " BlueMap markers for quest " + quest.getId());
			});
		});
	}
	
	@Override
	public void removeMarker(Quest quest) {
		BlueMapAPI.getInstance().ifPresent(api -> {
			Location lc = quest.getOptionValueOrDef(OptionStarterNPC.class).getLocation();
			api.getWorld(lc.getWorld()).map(BlueMapWorld::getMaps).ifPresent(maps -> {
				for (int i = 0; i < maps.size(); i++) {
					set.getMarkers().remove("qu_" + quest.getId() + "_" + i);
				}
			});
		});
	}

}
