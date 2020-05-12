package fr.skytasul.quests.requirements;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.TargetNumberRequirement;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.ScoreboardObjectiveParser;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.utils.Lang;

public class ScoreboardRequirement extends TargetNumberRequirement {

	private Objective objective;
	private String objectiveName;

	public ScoreboardRequirement() {
		super("scoreboardRequired");
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
	protected void save(Map<String, Object> datas) {
		super.save(datas);
		datas.put("objective", objectiveName);
	}

	@Override
	protected void load(Map<String, Object> savedDatas) {
		super.load(savedDatas);
		setObjectiveName((String) savedDatas.get("objective"));
	}

	public static class Creator extends TargetNumberRequirement.Creator<ScoreboardRequirement> {

		@Override
		public void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui) {
			Lang.CHOOSE_SCOREBOARD_OBJECTIVE.send(p);
			new TextEditor(p, (obj) -> {
				Objective objective = (Objective) obj;
				datas.put("objectiveName", objective.getName());
				super.itemClick(p, datas, gui);
			}, new ScoreboardObjectiveParser()).enterOrLeave(p);
			super.itemClick(p, datas, gui);
		}

		@Override
		public void edit(Map<String, Object> datas, AbstractRequirement requirement) {
			super.edit(datas, requirement);
			datas.put("objectiveName", ((ScoreboardRequirement) requirement).objectiveName);
		}

		@Override
		public AbstractRequirement finish(Map<String, Object> datas) {
			ScoreboardRequirement requirement = new ScoreboardRequirement();
			requirement.setObjectiveName((String) datas.get("objectiveName"));
			return super.finish(requirement, datas);
		}

		@Override
		public Class<? extends Number> numberClass() {
			return Double.class;
		}

		@Override
		public void sendHelpString(Player p) {
			Lang.CHOOSE_SCOREBOARD_TARGET.send(p);
		}

	}

}
