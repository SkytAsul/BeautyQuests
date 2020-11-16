package fr.skytasul.quests.utils;

import fr.skytasul.quests.BeautyQuests;

public class DebugUtils {

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

	
}
