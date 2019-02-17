package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.McCombatLevel;

public class McCombatLevelRequirement extends AbstractRequirement {

	public int level;

	public McCombatLevelRequirement(){
		super("mcmmoCombatLevelRequirement");
		if (!Dependencies.mmo) throw new MissingDependencyException("McCombatLevel");
	}

	public boolean test(Player p){
		return McCombatLevel.getCombatLevel(p) >= level;
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_COMBAT_LEVEL.send(p, level);
	}

	
	protected void save(Map<String, Object> datas) {
		datas.put("level", level);
	}

	
	protected void load(Map<String, Object> savedDatas) {
		level = (int) savedDatas.get("level");
	}

}
