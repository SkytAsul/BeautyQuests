package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.McMMO;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class McMMOSkillRequirement extends TargetNumberRequirement {

	public String skillName;

	public McMMOSkillRequirement(){
		super("mcmmoSklillLevelRequired");
		if (!DependenciesManager.mmo) throw new MissingDependencyException("mcMMO");
	}

	@Override
	public double getPlayerTarget(Player p) {
		return McMMO.getLevel(p, skillName);
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_SKILL.send(p, (int) super.target, skillName);
	}
	
	protected void save(Map<String, Object> datas) {
		super.save(datas);
		datas.put("skillName", skillName);
	}
	
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		skillName = (String) savedDatas.get("skillName");
		if (savedDatas.containsKey("level")) super.target = (int) savedDatas.get("level");
	}

	public static class Creator extends TargetNumberRequirement.Creator<McMMOSkillRequirement> {
		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			Lang.CHOOSE_SKILL_REQUIRED.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				if (datas.containsKey("skill")) {
					datas.remove("lvl");
					datas.remove("skill");
				}
				datas.put("skill", obj);
				super.itemClick(p, datas, gui);
			}));
		}

		public McMMOSkillRequirement finish(Map<String, Object> datas) {
			McMMOSkillRequirement req = new McMMOSkillRequirement();
			req.skillName = (String) datas.get("skill");
			return super.finish(req, datas);
		}

		public void edit(Map<String, Object> datas, McMMOSkillRequirement requirement) {
			super.edit(datas, requirement);
			datas.put("skill", requirement.skillName);
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
