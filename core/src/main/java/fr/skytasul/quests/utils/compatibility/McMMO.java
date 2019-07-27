package fr.skytasul.quests.utils.compatibility;

import org.bukkit.entity.Player;

import com.gmail.nossr50.api.ExperienceAPI;

public class McMMO {

	public static int getLevel(Player p, String jobName){
		return ExperienceAPI.getLevel(p, jobName);
	}
	
}
