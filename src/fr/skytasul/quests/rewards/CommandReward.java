package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.rewards.AbstractReward;

public class CommandReward extends AbstractReward {

	public String cmd;
	public boolean console;
	
	public CommandReward(){
		super("commandReward");
	}
	
	public CommandReward(String cmd, boolean console){
		this();
		this.cmd = cmd;
		this.console = console;
	}

	
	public String give(Player p){
		if (cmd == null) return null;
		String formattedcmd = this.cmd.replace("{PLAYER}", p.getName());
		if (console){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedcmd);
		}else p.performCommand(formattedcmd);
		return null;
	}
	
	public String toString(){
		return "/" + cmd + " (" + (console ? "console" : "player") + ")";
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("command", cmd);
		datas.put("console", console);
	}

	
	protected void load(Map<String, Object> savedDatas){
		cmd = (String) savedDatas.get("command");
		console = (boolean) savedDatas.get("console");
	}

}
