package fr.skytasul.quests.utils.types;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.QuestsPlaceholders;

public class Command {

	public final String label;
	public final boolean console;
	public final boolean parse;
	public final int delay;
	
	public Command(String label, boolean console, boolean parse, int delay) {
		this.label = label;
		this.console = console;
		this.parse = parse;
		this.delay = delay;
	}
	
	public void execute(Player o){
		Runnable run = () -> {
			String formattedcmd = label.replace("{PLAYER}", o.getName());
			if (parse && DependenciesManager.papi.isEnabled()) formattedcmd = QuestsPlaceholders.setPlaceholders(o, formattedcmd);
			CommandSender sender = console ? Bukkit.getConsoleSender() : o;
			Bukkit.dispatchCommand(sender, formattedcmd);
			DebugUtils.logMessage(sender.getName() + " performed command " + formattedcmd);
		};
		if (delay == 0 && Bukkit.isPrimaryThread()) {
			run.run();
		}else Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), run, delay);
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		map.put("label", label);
		map.put("console", console);
		if (parse) map.put("parse", parse);
		if (delay > 0) map.put("delay", delay);
		
		return map;
	}
	
	public static Command deserialize(Map<String, Object> map){
		return new Command((String) map.get("label"), (boolean) map.get("console"), (boolean) map.getOrDefault("parse", Boolean.FALSE), (int) map.getOrDefault("delay", 0));
	}
	
}
