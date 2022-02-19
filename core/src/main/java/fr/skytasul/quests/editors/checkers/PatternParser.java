package fr.skytasul.quests.editors.checkers;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;

public class PatternParser implements AbstractParser<Pattern> {
	
	public static final PatternParser PARSER = new PatternParser();
	
	private PatternParser() {}
	
	@Override
	public Pattern parse(Player p, String msg) throws Throwable {
		try {
			return Pattern.compile(msg);
		}catch (PatternSyntaxException ex) {
			Lang.INVALID_PATTERN.send(p, msg);
			return null;
		}
	}
	
}
