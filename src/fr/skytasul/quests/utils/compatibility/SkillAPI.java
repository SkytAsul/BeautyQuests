package fr.skytasul.quests.utils.compatibility;

import org.bukkit.entity.Player;

import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.enums.ExpSource;

public class SkillAPI {

	public static void giveExp(Player p, int xp){
		com.sucy.skill.SkillAPI.getPlayerData(p).giveExp(xp, ExpSource.QUEST);
	}

	public static boolean classExists(String name){
		return getClass(name) != null;
	}
	
	public static RPGClass getClass(String name) {
		return com.sucy.skill.SkillAPI.getClass(name);
	}
	
}
