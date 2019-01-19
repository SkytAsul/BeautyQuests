package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.McMMO;

public class McMMOSkillRequirement extends AbstractRequirement {

	public String skillName;
	public int level;

	public McMMOSkillRequirement(){
		super("mcmmoSklillLevelRequired");
		if (!Dependencies.mmo) throw new MissingDependencyException("mcMMO");
	}

	public boolean test(Player p){
		return McMMO.getLevel(p, skillName) >= level;
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_SKILL.send(p, level, skillName);
	}

	
	protected void save(Map<String, Object> datas) {
		datas.put("skillName", skillName);
		datas.put("level", level);
	}

	
	protected void load(Map<String, Object> savedDatas) {
		skillName = (String) savedDatas.get("skillName");
		level = (int) savedDatas.get("level");
	}

}
