package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.live.bemmamin.gps.api.GPSAPI;

import fr.skytasul.quests.BeautyQuests;

public class GPS {

	private static GPSAPI api;
	
	static void init(){
		api = new GPSAPI(BeautyQuests.getInstance());
	}
	
	public static boolean launchCompass(Player p, Location location) {
		if (api.gpsIsActive(p)) return false;
		api.startCompass(p, location);
		return true;
	}
	
	public static void stopCompass(Player p){
		api.stopGPS(p);
	}
	
}
