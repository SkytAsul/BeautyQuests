package fr.skytasul.quests.api.editors.parsers;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class PatternParser implements AbstractParser<Pattern> {
	
	public static final PatternParser PARSER = new PatternParser();
	
	private PatternParser() {}
	
	@Override
	public Pattern parse(Player p, String msg) throws Throwable {
		try {
			return Pattern.compile(msg);
		}catch (PatternSyntaxException ex) {
			Lang.INVALID_PATTERN.send(p, PlaceholderRegistry.of("input", msg));
			return null;
		}
	}
	
}
