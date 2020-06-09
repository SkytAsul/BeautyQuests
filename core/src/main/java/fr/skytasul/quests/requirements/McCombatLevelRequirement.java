package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.McCombatLevel;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class McCombatLevelRequirement extends TargetNumberRequirement {

	public McCombatLevelRequirement(){
		super("mcmmoCombatLevelRequirement");
		if (!DependenciesManager.mmo) throw new MissingDependencyException("McCombatLevel");
	}

	@Override
	public double getPlayerTarget(Player p) {
		return McCombatLevel.getCombatLevel(p);
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_COMBAT_LEVEL.send(p, (int) super.target);
	}
	
	protected void save(Map<String, Object> datas) {
		super.save(datas);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		if (savedDatas.containsKey("level")) super.target = (int) savedDatas.get("level");
	}

	public static class Creator extends TargetNumberRequirement.Creator<McCombatLevelRequirement> {

		public McCombatLevelRequirement finish(Map<String, Object> datas) {
			return super.finish(new McCombatLevelRequirement(), datas);
		}

		@Override
		public Class<? extends Number> numberClass() {
			return Integer.class;
		}

		@Override
		public void sendHelpString(Player p) {
			Lang.CHOOSE_XP_REQUIRED.send(p);
		}
	}

}
