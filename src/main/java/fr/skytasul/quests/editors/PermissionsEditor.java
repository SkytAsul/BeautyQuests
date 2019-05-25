package fr.skytasul.quests.editors;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;

public class PermissionsEditor extends Editor{
	
	protected RunnableObj run;
	
	private Map<String, Boolean> perms;
	
	public PermissionsEditor(Player p, RunnableObj end, Map<String, Boolean> permissions){
		super(p);
		this.run = end;
		this.perms = permissions;
	}

	public boolean chat(String message){
		String[] args = message.split(" ");
		String cmd = args[0];
		try{
			Command.valueOf(cmd.toUpperCase());
		}catch (IllegalArgumentException ex){
			Utils.sendMessage(p, Lang.COMMAND_DOESNT_EXIST_NOSLASH.toString());
			return false;
		}
		Command comd = Command.valueOf(cmd.toUpperCase());
		switch (comd){
		
		case ADD:
			if (args.length < 2){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + " add <permission> [remove]");
				break;
			}
			perms.put(args[1], args.length < 3 ? false : Boolean.parseBoolean(args[2]));
			Lang.TEXTLIST_TEXT_ADDED.send(p, args[1]);
			break;

		case REMOVE:
			if (args.length < 2){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + " remove <permission>");
				break;
			}
			perms.remove(args[1]);
			Utils.sendMessage(p, Lang.TEXTLIST_TEXT_REMOVED.toString(), args[1]);
			break;

		case LIST:
			StringBuilder stb = new StringBuilder("§6§lList : §r§e(separator : \"§6§l|§r§e\")\n");
			for (Entry<String, Boolean> en : perms.entrySet()){
				stb.append("§r§a\"" + en.getKey() + "§r§a\", §oremove: " + en.getValue() + " §6§l| ");
			}
			p.sendMessage(stb.toString());
			break;

		case HELP:
			p.sendMessage("§aadd <permission> [remove] : add a permission\n"
					+ "remove <permission> : remove a permission\n"
					+ "list : view all permissions\n"
					+ "close : validate");
			break;

		case CLOSE:
			leave(p);
			run.run(perms);
			break;

		}
		return true;
	}
	
	protected String cancelWord(){
		return "close";
	}

	private enum Command{
		ADD, REMOVE, LIST, HELP, CLOSE;
	}
	
}
