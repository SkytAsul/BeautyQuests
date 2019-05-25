package fr.skytasul.quests.editors;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;
import fr.skytasul.quests.utils.types.RunnableReturn;

public class TextListEditor extends Editor{
	
	protected RunnableObj run;
	public RunnableReturn<Boolean> valid;
	
	private List<String> texts;
	
	public TextListEditor(Player p, RunnableObj end, List<String> texts){
		super(p);
		Validate.notNull(texts, "Text list in Editor cannot be null.");
		this.run = end;
		this.texts = texts;
	}

	public boolean chat(String message){
		String[] args = message.split(" ");
		String msg = "";
		boolean hasMsg = false;
		String cmd = args[0];
		try{
			Command.valueOf(cmd.toUpperCase());
		}catch (IllegalArgumentException ex){
			Utils.sendMessage(p, Lang.COMMAND_DOESNT_EXIST_NOSLASH.toString());
			return false;
		}
		if (args.length > 1){
			msg = Utils.buildFromArray(args, 1, " ");
			hasMsg = true;
		}
		Command comd = Command.valueOf(cmd.toUpperCase());
		switch (comd){
		
		case ADD:
			if (!hasMsg){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + " add <message>");
				break;
			}
			if (valid != null){
				if (!valid.run(msg)) break;
			}
			texts.add(msg);
			Lang.TEXTLIST_TEXT_ADDED.send(p, msg);
			break;

		case REMOVE:
			if (!hasMsg){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + " remove <id>");
				break;
			}
			try{
				Utils.sendMessage(p, Lang.TEXTLIST_TEXT_REMOVED.toString(), texts.remove(Integer.parseInt(args[1])));
			}catch (IllegalArgumentException ex){
				Utils.sendMessage(p, Lang.NUMBER_INVALID.toString());
			}
			break;

		case LIST:
			StringBuilder stb = new StringBuilder("§6§lList : §r§e(separator : \"§6§l|§r§e\")\n");
			for (String s : texts){
				stb.append("§r§a" + s + " §6§l| ");
			}
			p.sendMessage(stb.toString());
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.textList.help.")) l.sendWP(p);
			}
			break;

		case CLOSE:
			leave(p);
			run.run(texts);
			break;

		}
		return true;
	}

	private enum Command{
		ADD, REMOVE, LIST, HELP, CLOSE;
	}
	
}
