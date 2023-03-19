package fr.skytasul.quests.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import fr.skytasul.quests.BeautyQuests;

public class DebugUtils {

	private DebugUtils() {}
	
	private static Map<String, Long> errors = new HashMap<>();
	
	public static void logMessage(String msg){
		BeautyQuests.getInstance().getLoggerHandler().write(msg, "DEBUG");
	}
	
	public static String stackTraces(int from, int to){
		StringJoiner joiner = new StringJoiner(" -> ");
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (int i = from + 1; i <= to && i < stack.length; i++) {
			joiner.add(stack[from].getClassName().replace("fr.skytasul.quests", "f.s.q") + "." + stack[from].getMethodName()
					+ " " + stack[from].getLineNumber());
		}
		return joiner.toString();
	}

	public static void printError(String errorMsg, String typeID, int seconds) {
		Long time = errors.get(typeID);
		if (time == null || time.longValue() + seconds * 1000 < System.currentTimeMillis()) {
			BeautyQuests.logger.warning(errorMsg);
			errors.put(typeID, System.currentTimeMillis());
		}
	}
	
}
