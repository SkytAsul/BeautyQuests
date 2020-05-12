package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.Jobs;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class JobLevelRequirement extends TargetNumberRequirement {

	public String jobName;
	
	public JobLevelRequirement() {
		super("jobLevelRequired");
		if (!DependenciesManager.jobs) throw new MissingDependencyException("Jobs");
	}

	@Override
	public double getPlayerTarget(Player p) {
		return Jobs.getLevel(p, jobName);
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_JOB.send(p, (int) super.target, jobName);
	}
	
	protected void save(Map<String, Object> datas) {
		super.save(datas);
		datas.put("jobName", jobName);
	}

	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		jobName = (String) savedDatas.get("jobName");
		if (savedDatas.containsKey("level")) super.target = (int) savedDatas.get("level");
	}

	public static class Creator extends TargetNumberRequirement.Creator<JobLevelRequirement> {
		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			Lang.CHOOSE_JOB_REQUIRED.send(p);
			Editor.enterOrLeave(p, new TextEditor(p, (obj) -> {
				if (datas.containsKey("job")){
					datas.remove("lvl");
					datas.remove("job");
				}
				datas.put("job", obj);
				super.itemClick(p, datas, gui);
			}));
		}

		public AbstractRequirement finish(Map<String, Object> datas) {
			JobLevelRequirement req = new JobLevelRequirement();
			req.jobName = (String) datas.get("job");
			return super.finish(req, datas);
		}
		
		public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
			super.edit(datas, requirement);
			datas.put("job", ((JobLevelRequirement) requirement).jobName);
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