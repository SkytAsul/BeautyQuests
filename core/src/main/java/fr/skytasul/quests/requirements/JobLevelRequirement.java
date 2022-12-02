package fr.skytasul.quests.requirements;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.utils.ComparisonMethod;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.Jobs;

public class JobLevelRequirement extends TargetNumberRequirement {

	public String jobName;
	
	public JobLevelRequirement() {
		this(null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public JobLevelRequirement(String jobName, double target, ComparisonMethod comparison) {
		super(target, comparison);
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
		new TextEditor<String>(event.getPlayer(), event::cancel, obj -> {
			jobName = obj;
			super.itemClick(event);
		}).useStrippedMessage().enter();
	}
	
	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("jobName", jobName);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		jobName = section.getString("jobName");
	}
	
}