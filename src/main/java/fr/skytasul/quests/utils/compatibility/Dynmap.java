package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Utils;

public class Dynmap {

	private static DynmapAPI dynmap;
	private static MarkerAPI api;
	private static MarkerIcon icon;
	
	private static MarkerSet markers;
	
	public static void intitialize(){
		dynmap = (DynmapAPI) Bukkit.getPluginManager().getPlugin("dynmap");
		api = dynmap.getMarkerAPI();
		icon = api.getMarkerIcon(QuestsConfiguration.dynmapMarkerIcon());
		
		markers = api.getMarkerSet("beautyquests.markerset");
		if (markers == null){
			markers = api.createMarkerSet("beautyquests.markerset", QuestsConfiguration.dynmapSetName(), null, false);
		}else markers.setMarkerSetLabel(QuestsConfiguration.dynmapSetName());
		
		markers.setMinZoom(1);
		markers.setHideByDefault(false);
		markers.setDefaultMarkerIcon(icon);
	}
	
	public static void unload(){
		if (markers != null){
			markers.deleteMarkerSet();
			markers = null;
		}
	}
	
	public static void addMarker(Quest quest){
		if (markers == null) return;
		if (quest.getStarter() == null) return;
		if (quest.isHid()) {
			DebugUtils.logMessage("No marker created for quest " + quest.getID() + " : quest is hid");
			return;
		}
		Location lc = quest.getStarter().getStoredLocation();
		
		Marker marker = markers.createMarker("qu_" + quest.getID(), Utils.removeColors(quest.getName()), lc.getWorld().getName(), lc.getX(), lc.getBlockY(), lc.getBlockZ(), icon, false);
		
		if (marker == null) {
			BeautyQuests.logger.severe("Problem when creating marker for quest " + quest.getID());
		}else DebugUtils.logMessage("Marker " + marker.getMarkerID() + " created");
	}
	
	public static void removeMarker(Quest quest){
		if (markers == null) return;
		if (quest.isHid()) return;
		Marker marker = markers.findMarker("qu_" + quest.getID());
		if (marker == null) {
			BeautyQuests.logger.warning("Unable to find marker for quest " + quest.getID());
		}else marker.deleteMarker();
	}
	
}