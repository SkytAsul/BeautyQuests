package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MissingDependencyException;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Jobs;

public class JobLevelRequirement extends AbstractRequirement {

	public String jobName;
	public int level;
	
	public JobLevelRequirement() {
		super("jobLevelRequired");
		if (!Dependencies.jobs) throw new MissingDependencyException("Jobs");
	}

	public boolean test(Player p) {
		return Jobs.getLevel(p, jobName) >= level;
	}
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_JOB.send(p, level, jobName);
	}
	
	
	protected void save(Map<String, Object> datas) {
		datas.put("jobName", jobName);
		datas.put("level", level);
	}

	protected void load(Map<String, Object> savedDatas) {
		jobName = (String) savedDatas.get("jobName");
		level = (int) savedDatas.get("level");
	}

}