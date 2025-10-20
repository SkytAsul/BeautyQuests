package fr.skytasul.quests.integrations.maps;

import fr.skytasul.quests.api.AbstractMapIntegration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.integrations.IntegrationsConfiguration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

public class BQDynmap extends AbstractMapIntegration {

	private MarkerIcon icon;
	private MarkerSet markers;
	
	@Override
	public boolean isEnabled() {
		return IntegrationsConfiguration.getConfiguration().dynmapMarkerIcon() != null
				&& !IntegrationsConfiguration.getConfiguration().dynmapMarkerIcon().isEmpty();
	}

	@Override
	protected void initializeMarkers(Runnable initializeQuests) {
		DynmapAPI dynmap = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
		MarkerAPI api = dynmap.getMarkerAPI();
		icon = api.getMarkerIcon(IntegrationsConfiguration.getConfiguration().dynmapMarkerIcon());
		
		markers = api.getMarkerSet("beautyquests.markerset");
		if (markers == null){
			markers = api.createMarkerSet("beautyquests.markerset",
					IntegrationsConfiguration.getConfiguration().dynmapSetName(), null, false);
		} else
			markers.setMarkerSetLabel(IntegrationsConfiguration.getConfiguration().dynmapSetName());
		
		markers.setMinZoom(IntegrationsConfiguration.getConfiguration().dynmapMinimumZoom());
		markers.setHideByDefault(false);
		markers.setDefaultMarkerIcon(icon);
		
		initializeQuests.run();
	}
	
	@Override
	public void unload() {
		if (markers != null){
			markers.deleteMarkerSet();
			markers = null;
		}
	}
	
	@Override
	public void addMarker(Quest quest, Location lc) {
		if (markers == null) return;
		
		Marker marker = markers.createMarker("qu_" + quest.getId(), ChatColor.stripColor(quest.getName()), lc.getWorld().getName(), lc.getX(), lc.getBlockY(), lc.getBlockZ(), icon, false);
		
		if (marker == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Problem when creating marker for quest " + quest.getId());
		}else QuestsPlugin.getPlugin().getLoggerExpanded().debug("Marker " + marker.getMarkerID() + " created");
	}
	
	@Override
	public void removeMarker(Quest quest) {
		if (markers == null) return;
		
		Marker marker = markers.findMarker("qu_" + quest.getId());
		if (marker == null) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Unable to find marker for quest " + quest.getId());
		}else marker.deleteMarker();
	}
	
}