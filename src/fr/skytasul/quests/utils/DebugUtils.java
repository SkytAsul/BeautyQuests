package fr.skytasul.quests.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import fr.skytasul.quests.BeautyQuests;

public class DebugUtils {

	public static boolean debugMode = false;
	
	public static void debugMessage(CommandSender p, String msg){
		if (debugMode){
			 if (p == null) p = Bukkit.getConsoleSender();
			 p.sendMessage(msg);
		}
	}
	
	public static void broadcastDebugMessage(String msg){
		if (debugMode) Bukkit.broadcastMessage(msg);
		logMessage(msg);
	}

	public static void logMessage(String msg){
		BeautyQuests.logger.write("[DEBUG]: " + msg);
	}
	
	public static String stackTraces(int from, int to){
		from++;
		
		String s = "";
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (;from <= to; from++) {
			s = s + stack[from].getClassName() + "." + stack[from].getMethodName() + " ; ";
		}
		return s;
	}

	
}
