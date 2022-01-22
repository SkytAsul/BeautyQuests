package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.Jobs;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class JobLevelRequirement extends TargetNumberRequirement {

	public String jobName;
	
	public JobLevelRequirement() {
		this(null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public JobLevelRequirement(String jobName, double target, ComparisonMethod comparison) {
		super(target, comparison);
		if (!DependenciesManager.jobs.isEnabled()) throw new MissingDependencyException("Jobs");
		this.jobName = jobName;
	}

	@Override
	public double getPlayerTarget(Player p) {
		return Jobs.getLevel(p, jobName);
	}
	
	@Override
	public Class<? extends Number> numberClass() {
		return Integer.class;
	}
	
	@Override
	public void sendHelpString(Player p) {
		Lang.CHOOSE_XP_REQUIRED.send(p);
	}
	
	@Override
	public void sendReason(Player p){
		Lang.REQUIREMENT_JOB.send(p, getFormattedValue(), jobName);
	}
	
	@Override
	public String getDescription(Player p) {
		return Lang.RDJobLevel.format(Integer.toString((int) target), jobName);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new JobLevelRequirement(jobName, target, comparison);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { getValueLore(), "§8>Job name: §7" + jobName, "", Lang.RemoveMid.toString() };
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_JOB_REQUIRED.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), () -> {
			if (jobName == null) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			jobName = obj;
			event.updateItemLore(getLore());
			super.itemClick(event);
		}).useStrippedMessage().enter();
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		super.save(datas);
		datas.put("jobName", jobName);
	}

	@Override
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		jobName = (String) savedDatas.get("jobName");
		if (savedDatas.containsKey("level")) super.target = (int) savedDatas.get("level");
	}
	
}