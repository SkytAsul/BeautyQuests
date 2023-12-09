package fr.skytasul.quests.api.editors.parsers;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EnumParser<T extends Enum<T>> extends CollectionParser<T> {

	private static final Pattern FORMAT = Pattern.compile("[ _]");
	
	public EnumParser(Class<T> enumClass) {
		super(Arrays.asList(enumClass.getEnumConstants()), constant -> processConstantName(constant.name()));
	}
	
	public EnumParser(Class<T> enumClass, Predicate<T> filter) {
		super(Arrays.stream(enumClass.getEnumConstants()).filter(filter).collect(Collectors.toList()), constant -> processConstantName(constant.name()));
	}
	
	@Override
	protected String processName(String msg) {
		return processConstantName(msg);
	}
	
	static String processConstantName(String key) {
		return FORMAT.matcher(key.toLowerCase()).replaceAll("");
	}

}
