package fr.skytasul.quests.editors.checkers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;

import fr.skytasul.quests.utils.Lang;

public class ScoreboardObjectiveParser implements AbstractParser {

	@Override
	public Object parse(Player p, String msg) throws Throwable {
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(msg);
		if (objective == null) Lang.SCOREBOARD_OBJECTIVE_NOT_FOUND.send(p);
		return objective;
	}

}
