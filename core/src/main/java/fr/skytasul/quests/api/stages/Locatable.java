package fr.skytasul.quests.api.stages;

import org.bukkit.Location;

public interface Locatable {
	
	Location getLocation();
	
	boolean isShown();
	
}
