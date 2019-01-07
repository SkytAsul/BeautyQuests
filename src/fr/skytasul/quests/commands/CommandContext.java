package fr.skytasul.quests.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandContext {

	public final CommandsManager manager;
	public final Object[] args;
	public final String label;
	public final CommandSender sender;
	public final Player player;
	
	public CommandContext(CommandsManager manager, CommandSender sender, Object[] args, String label){
		this.manager = manager;
		this.args = args;
		this.label = label;
		this.sender = sender;
		if (sender instanceof Player){
			this.player = (Player) sender;
		}else this.player = null;
	}
	
	public boolean isPlayer(){
		return player != null;
	}
	
}