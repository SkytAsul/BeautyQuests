package fr.skytasul.quests.utils;

import java.util.HashMap;
import java.util.Map;

import fr.skytasul.quests.BeautyQuests;

public class DebugUtils {

	private DebugUtils() {}
	
	private static Map<String, Long> errors = new HashMap<>();
	
	public static void logMessage(String msg){
		BeautyQuests.logger.write("[DEBUG]: " + msg);
	}
	
	public static String stackTraces(int from, int to){
		from++;
		
		String s = "";
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (;from <= to; from++) {
			s = s + stack[from].getClassName() + "." + stack[from].getMethodName() + " " + stack[from].getLineNumber() + "; ";
		}
		return s;
	}

	public static void printError(String errorMsg, String typeID, int seconds) {
		Long time = errors.get(typeID);
		if (time == null || time.longValue() + seconds * 1000 < System.currentTimeMillis()) {
			BeautyQuests.logger.warning(errorMsg);
			errors.put(typeID, System.currentTimeMillis());
		}
	}
	
}
