package fr.skytasul.quests.api.editors.parsers;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.jetbrains.annotations.Nullable;

import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class PatternParser implements AbstractParser<Pattern> {
	
	public static final PatternParser PARSER = new PatternParser();
	
	private PatternParser() {}
	
	@Override
	public Pattern parse(String msg) throws ParsingError {
		try {
			return Pattern.compile(msg);
		}catch (PatternSyntaxException ex) {
			throw new ParsingError(Lang.INVALID_PATTERN.format(PlaceholderRegistry.of("input", msg)));
		}
	}

	@Override
	public @Nullable String getIndication() {
		return Lang.TEXT_PARSER_REGEX.toString();
	}
	
}
