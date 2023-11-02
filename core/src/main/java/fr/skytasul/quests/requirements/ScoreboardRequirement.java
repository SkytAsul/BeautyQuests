package fr.skytasul.quests.requirements;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.editors.parsers.ScoreboardObjectiveParser;
import fr.skytasul.quests.api.gui.LoreBuilder;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectClickEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.api.utils.ComparisonMethod;

public class ScoreboardRequirement extends TargetNumberRequirement {

	private Objective objective;
	private String objectiveName;

	public ScoreboardRequirement() {
		this(null, null, null, 0, ComparisonMethod.GREATER_OR_EQUAL);
	}

	public ScoreboardRequirement(String customDescription, String customReason, String objectiveName, double target,
			ComparisonMethod comparison) {
		super(customDescription, customReason, target, comparison);

		if (objectiveName != null)
			setObjectiveName(objectiveName);
	}

	@Override
	public double getPlayerTarget(Player p) {
		return objective.getScore(p.getName()).getScore();
	}

	@Override
	public boolean isValid() {
		return objective != null;
	}

	@Override
	protected @NotNull String getInvalidReason() {
		return "cannot find scoreboard objective " + objectiveName;
	}

	private void setObjectiveName(String name) {
		objectiveName = name;
		objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(name);
		if (objective == null)
			QuestsPlugin.getPlugin().getLogger().warning("Cannot find scoreboard objective " + name);
	}

	@Override
	public Class<? extends Number> numberClass() {
		return Double.class;
	}

	@Override
	protected String getPlaceholderName() {
		return "score";
	}

	@Override
	public void sendHelpString(Player p) {
		Lang.CHOOSE_SCOREBOARD_TARGET.send(p);
	}

	@Override
	protected void addLore(LoreBuilder loreBuilder) {
		super.addLore(loreBuilder);
		loreBuilder.addDescription("§8Objective name: §7" + objectiveName);
	}

	@Override
	public void itemClick(QuestObjectClickEvent event) {
		Lang.CHOOSE_SCOREBOARD_OBJECTIVE.send(event.getPlayer());
		new TextEditor<>(event.getPlayer(), () -> {
			if (objectiveName == null) event.getGUI().remove(this);
			event.reopenGUI();
		}, obj -> {
			this.objective = obj;
			this.objectiveName = objective.getName();
			super.itemClick(event);
		}, () -> {
			event.getGUI().remove(this);
			event.reopenGUI();
		}, new ScoreboardObjectiveParser()).start();
	}

	@Override
	public void save(ConfigurationSection section) {
		super.save(section);
		section.set("objective", objectiveName);
	}

	@Override
	public void load(ConfigurationSection section) {
		super.load(section);
		setObjectiveName(section.getString("objective"));
	}

	@Override
	public AbstractRequirement clone() {
		return new ScoreboardRequirement(getCustomDescription(), getCustomReason(), objectiveName, target, comparison);
	}

}
