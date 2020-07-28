package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.DebugUtils;

public class Command {

	public final String label;
	public final boolean console;
	public final int delay;
	
	public Command(String label, boolean console, int delay) {
		this.label = label;
		this.console = console;
		this.delay = delay;
	}
	
	public void execute(Player o){
		Runnable run = () -> {
			String formattedcmd = label.replace("{PLAYER}", o.getName());
			if (console) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedcmd);
			}else o.performCommand(formattedcmd);
			DebugUtils.logMessage((console ? "Console" : o.getName()) + " just performed command " + formattedcmd);
		};
		if (delay == 0) {
			run.run();
		}else Bukkit.getScheduler().scheduleSyncDelayedTask(BeautyQuests.getInstance(), run, delay);
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		map.put("label", label);
		map.put("console", console);
		map.put("delay", delay);
		
		return map;
	}
	
	public static Command deserialize(Map<String, Object> map){
		return new Command((String) map.get("label"), (boolean) map.get("console"), map.containsKey("delay") ? (int) map.get("delay") : 0);
	}
	
}
