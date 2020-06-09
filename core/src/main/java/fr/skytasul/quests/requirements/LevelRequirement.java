package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.utils.Lang;

public class LevelRequirement extends TargetNumberRequirement {
	
	public LevelRequirement() {
		super("levelRequired");
	}

	@Override
	public double getPlayerTarget(Player p) {
		return p.getLevel();
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_LEVEL.send(p, (int) super.target);
	}
	
	protected void save(Map<String, Object> datas) {
		super.save(datas);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		if (savedDatas.containsKey("level")) super.target = (int) savedDatas.get("level");
	}

	public static class Creator extends TargetNumberRequirement.Creator<LevelRequirement> {

		public LevelRequirement finish(Map<String, Object> datas) {
			return super.finish(new LevelRequirement(), datas);
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
