package fr.skytasul.quests.api.editors.checkers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import fr.skytasul.quests.api.localization.Lang;

public class ScoreboardObjectiveParser implements AbstractParser<Objective> {

	@Override
	public Objective parse(Player p, String msg) throws Throwable {
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(msg);
		if (objective == null) Lang.SCOREBOARD_OBJECTIVE_NOT_FOUND.send(p);
		return objective;
	}

}
