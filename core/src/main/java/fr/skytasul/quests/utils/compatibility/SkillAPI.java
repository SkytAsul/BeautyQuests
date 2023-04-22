package fr.skytasul.quests.utils.compatibility;

import java.util.Collection;
import org.bukkit.entity.Player;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.enums.ExpSource;

public class SkillAPI {

	public static void giveExp(Player p, int xp){
		com.sucy.skill.SkillAPI.getPlayerData(p).giveExp(xp, ExpSource.QUEST);
	}

	public static int getLevel(Player p) {
		return com.sucy.skill.SkillAPI.getPlayerData(p).getMainClass().getLevel();
	}
	
	public static boolean classExists(String name){
		return getClass(name) != null;
	}
	
	public static RPGClass getClass(String name) {
		return com.sucy.skill.SkillAPI.getClass(name);
	}
	
	public static Collection<RPGClass> getClasses() {
		return com.sucy.skill.SkillAPI.getClasses().values();
	}
	
}
