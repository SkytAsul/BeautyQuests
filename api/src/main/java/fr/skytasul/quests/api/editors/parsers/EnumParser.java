package fr.skytasul.quests.api.editors.parsers;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnumParser<T extends Enum<T>> extends CollectionParser<T> {

	private static final Pattern FORMAT = Pattern.compile("[ _]");
	
	public EnumParser(Class<T> enumClass) {
		this(enumClass, __ -> true);
	}
	
	public EnumParser(Class<T> enumClass, Predicate<T> filter) {
		super(Arrays.stream(enumClass.getEnumConstants()).filter(filter).toList());
	}
	
	@Override
	protected String processName(String msg) {
		return processConstantName(msg);
	}

	@Override
	public @Nullable String serialize(@NotNull T value) {
		return processConstantName(value.name());
	}
	
	static String processConstantName(String key) {
		return FORMAT.matcher(key.toLowerCase()).replaceAll("");
	}

}
