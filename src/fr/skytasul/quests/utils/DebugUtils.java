package fr.skytasul.quests.utils;

import org.apache.commons.lang.StringUtils;
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
		
		/*int i = 0;
		for (StackTraceElement stack : ){
			if (i < from) continue;
			s = s + stack.getClassName() + "." + stack.getMethodName() + " ; ";
			i++;
			if (i > to) break;
		}*/
		return s;
	}
	
	public static String stackTraceMessage(Throwable e, String message){
		return stackTraceMessage(e, message, false);
	}
	
	public static String stackTraceMessage(Throwable e, String message, boolean console){
		String s = "§c" + message + " Error " + ((e.getCause() != null) ? e.getCause().getClass().getName() : "null") + (StringUtils.isEmpty(e.getMessage()) ? "" : " | Message : §4" + e.getMessage()) + "§c | StackTraces :\n";
		for (StackTraceElement st : e.getStackTrace()) s = s + "\n" + st.getMethodName() + " §8§lIN §r" + st.getClassName() + " §8§lAT LINE §r" + st.getLineNumber();
		if (console){
			int ss = s.indexOf("§");
			while (ss != -1){
				s = s.substring(0, ss) + s.substring(ss + 2);
				ss = s.indexOf("§");
			}
		}
		return s;
	}

	public static void sendStackTrace(CommandSender sender, Throwable e, String message){
		/*p.sendMessage("§c" + message + " Error " + ((e.getCause() != null) ? e.getCause().getClass().getName() : "null") + (StringUtils.isEmpty(e.getMessage()) ? "" : " | Message : §4" + e.getMessage()) + "§c | StackTraces :\n");
		for (StackTraceElement st : e.getStackTrace()) p.sendMessage(st.getMethodName() + " §8in §r" + st.getClassName() + " §8at line §r" + st.getLineNumber());*/
		sender.sendMessage(stackTraceMessage(e, message));
	}
	
}
