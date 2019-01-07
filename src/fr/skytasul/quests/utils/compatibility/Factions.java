package fr.skytasul.quests.utils.compatibility;

import com.massivecraft.factions.entity.FactionColl;

public class Factions {

	public static boolean factionExists(String name){
		return getFaction(name) != null;
	}
	
	public static Object getFaction(String name){
		return FactionColl.get().getByName(name);
	}
	
}
