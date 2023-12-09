package fr.skytasul.quests.integrations.jobs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.api.utils.ComparisonMethod;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class JobLevelRequirement extends TargetNumberRequirement {

	public String jobName;
	
	public JobLevelRequirement() {
		this(null, null, null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}
	
	public JobLevelRequirement(String customDescription, String customReason, String jobName, double target,
			ComparisonMethod comparison) {
		super(customDescription, customReason, target, comparison);
		this.jobName = jobName;
	}

	@Override
	public double getPlayerTarget(Player p) {
		JobsPlayer player = Jobs.getPlayerManager().getJobsPlayer(p);
		if (player == null)
			return -1;
		JobProgression prog = player.getJobProgression(Jobs.getJob(jobName));
		if (prog == null)
			return 0;
		return prog.getLevel();
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
	protected String getPlaceholderName() {
		return "level";
	}

	@Override
	protected void createdPlaceholdersRegistry(@NotNull PlaceholderRegistry placeholders) {
		super.createdPlaceholdersRegistry(placeholders);
		placeholders.registerIndexed("job_name", () -> jobName);
	}

	@Override
	protected String getDefaultReason(Player player) {
		return Lang.REQUIREMENT_JOB.format(this);
	}
	
	@Override
	public String getDefaultDescription(Player p) {
		return Lang.RDJobLevel.format(this);
	}

	@Override
	public AbstractRequirement clone() {
		return new JobLevelRequirement(getCustomDescription(), getCustomReason(), jobName, target, comparison);
	}
	
	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription("§8Job name: §7" + jobName);
	}
	
	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_JOB_REQUIRED.send(event.getPlayer());
		new TextEditor<String>(event.getPlayer(), event::cancel, obj -> {
			jobName = obj;
			super.itemClick(event);
		}).useStrippedMessage().start();
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