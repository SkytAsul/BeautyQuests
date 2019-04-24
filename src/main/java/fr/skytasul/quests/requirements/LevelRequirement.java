package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.Lang;

public class LevelRequirement extends AbstractRequirement {

	public int level;
	
	public LevelRequirement() {
		super("levelRequired");
	}
	
	public boolean test(Player p) {
		return p.getLevel() >= level;
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_LEVEL.send(p, level);
	}
	
	
	protected void save(Map<String, Object> datas) {
		datas.put("level", level);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		level = (int) savedDatas.get("level");
	}

}
