package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Command {

	public String label;
	public boolean console;
	
	public Command(String label, boolean console){
		this.label = label;
		this.console = console;
	}
	
	public void execute(Player o){
		String formattedcmd = label.replace("{PLAYER}", o.getName());
		if (console){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedcmd);
		}else o.performCommand(formattedcmd);
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		map.put("label", label);
		map.put("console", console);
		
		return map;
	}
	
	public static Command deserialize(Map<String, Object> map){
		return new Command((String) map.get("label"), (boolean) map.get("console"));
	}
	
}
