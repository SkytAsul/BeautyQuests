package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.Jobs;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class JobLevelRequirement extends TargetNumberRequirement {

	public String jobName;
	
	public JobLevelRequirement() {
		this(null, 0);
	}
	
	public JobLevelRequirement(String jobName, double target) {
		super("jobLevelRequired", target);
		if (!DependenciesManager.jobs) throw new MissingDependencyException("Jobs");
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
	
	public void sendReason(Player p){
		Lang.REQUIREMENT_JOB.send(p, getFormattedValue(), jobName);
	}
	
	@Override
	public AbstractRequirement clone() {
		return new JobLevelRequirement(jobName, target);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { getValueLore(), "§8>Job name: §7" + jobName, "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_JOB_REQUIRED.send(p);
		new TextEditor<String>(p, () -> {
			if (jobName == null) gui.remove(this);
			gui.reopen();
		}, obj -> {
			jobName = obj;
			ItemUtils.lore(clicked, getLore());
			super.itemClick(p, gui, clicked);
		}).useStrippedMessage().enter();
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
	
}