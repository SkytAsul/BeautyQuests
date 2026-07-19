package fr.skytasul.quests.api.editors.parsers;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import fr.skytasul.quests.api.localization.Lang;

public class ScoreboardObjectiveParser implements AbstractParser<Objective> {

	@Override
	public Objective parse(String msg) throws ParsingError {
		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(msg);
		if (objective == null)
			throw new ParsingError(Lang.SCOREBOARD_OBJECTIVE_NOT_FOUND.format());
		return objective;
	}

}
