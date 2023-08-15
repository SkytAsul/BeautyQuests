package fr.skytasul.quests.utils;

import java.util.StringJoiner;

public class DebugUtils {

	private DebugUtils() {}
	
	public static String stackTraces(int from, int to){
		StringJoiner joiner = new StringJoiner(" -> ");
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (int i = from + 1; i <= to && i < stack.length; i++) {
			joiner.add(stack[from].getClassName().replace("fr.skytasul.quests", "f.s.q") + "." + stack[from].getMethodName()
					+ " " + stack[from].getLineNumber());
		}
		return joiner.toString();
	}
	
}
