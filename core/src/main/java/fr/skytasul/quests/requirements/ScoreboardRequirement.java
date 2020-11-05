package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;

import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.ScoreboardObjectiveParser;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.utils.Lang;

public class ScoreboardRequirement extends TargetNumberRequirement {

	private Objective objective;
	private String objectiveName;

	public ScoreboardRequirement() {
		this(null, 0);
	}
	
	public ScoreboardRequirement(String objectiveName, double target) {
		super("scoreboardRequired", target);
		if (objectiveName != null) this.objectiveName = objectiveName;
	}

	@Override
	public double getPlayerTarget(Player p) {
		return objective.getScore(p.getName()).getScore();
	}

	private void setObjectiveName(String name) {
		objectiveName = name;
		objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(name);
	}

	@Override
	public Class<? extends Number> numberClass() {
		return Double.class;
	}
	
	@Override
	public void sendHelpString(Player p) {
		Lang.CHOOSE_SCOREBOARD_TARGET.send(p);
	}
	
	@Override
	public String[] getLore() {
		return new String[] { getValueLore(), "§8>Objective name: §7" + objectiveName, "", Lang.Remove.toString() };
	}
	
	@Override
	public void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked) {
		Lang.CHOOSE_SCOREBOARD_OBJECTIVE.send(p);
		new TextEditor<>(p, () -> {
			if (objectiveName == null) gui.remove(this);
			gui.reopen();
		}, obj -> {
			this.objective = obj;
			this.objectiveName = objective.getName();
			super.itemClick(p, gui, clicked);
		}, () -> {
			gui.remove(this);
			gui.reopen();
		}, new ScoreboardObjectiveParser()).enter();
	}
	
	@Override
	protected void save(Map<String, Object> datas) {
		super.save(datas);
		datas.put("objective", objectiveName);
	}

	@Override
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		setObjectiveName((String) savedDatas.get("objective"));
	}

	@Override
	public AbstractRequirement clone() {
		return new ScoreboardRequirement(objectiveName, target);
	}

}
