package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.Message;
import fr.skytasul.quests.utils.types.Message.Sender;

public class DialogEditor extends Editor{
	
	private Runnable end;
	public Dialog d;

	public DialogEditor(Player p, Runnable end, Dialog dialog) {
		super(p, null);
		this.end = end;
		this.d = dialog;
	}

	@Override
	public boolean chat(String coloredMessage, String strippedMessage){
		String[] args = strippedMessage.split(" ");
		String[] argsColored = coloredMessage.split(" ");
		String msg = "";
		boolean hasMsg = false;
		Command cmd;
		try{
			cmd = Command.valueOf(args[0].toUpperCase());
		}catch (IllegalArgumentException ex){
			Utils.sendMessage(p, Lang.COMMAND_DOESNT_EXIST_NOSLASH.toString());
			return false;
		}
		if (args.length > 1){
			msg = Utils.buildFromArray(argsColored, 1, " ");
			hasMsg = true;
		}
		switch (cmd) {
		
		case NOSENDER:
		case NPC:
		case PLAYER:
			if (!hasMsg){
				Lang.DIALOG_SYNTAX.send(p, cmd, "");
				break;
			}
			d.add(msg, Sender.valueOf(cmd.name()));
			Utils.sendMessage(p, Lang.valueOf("DIALOG_MSG_ADDED_" + cmd.name()).toString(), msg, cmd.name().toLowerCase());
			break;

		case REMOVE:
			if (!hasMsg){
				Lang.DIALOG_REMOVE_SYNTAX.send(p);
				break;
			}
			try{
				Message removed = d.messages.remove(Integer.parseInt(args[1]));
				if (removed != null){
					Utils.sendMessage(p, Lang.DIALOG_MSG_REMOVED.toString(), removed.text);
				}else Lang.OUT_OF_BOUNDS.send(p, args[1], 0, d.messages.size());
			}catch (IllegalArgumentException ex){
				Utils.sendMessage(p, Lang.NUMBER_INVALID.toString());
			}
			break;

		case LIST:
			for (int i = 0; i < d.messages.size(); i++) {
				Message dmsg = d.messages.get(i);
				p.sendMessage("§6" + i + " :§a \"" + dmsg.text + "§r§a\"§e by §l" + dmsg.sender.name().toLowerCase());
			}
			break;
			
		case NOSENDERINSERT:
		case NPCINSERT:
		case PLAYERINSERT:
			if (args.length < 3){
				Lang.DIALOG_SYNTAX.send(p, cmd, " <id>");
				break;
			}
			try{
				msg = Utils.buildFromArray(argsColored, 2, " ");
				Sender sender = Sender.valueOf(cmd.name().replace("INSERT", ""));
				d.insert(msg, sender, Integer.parseInt(args[1]));
				Utils.sendMessage(p, Lang.valueOf("DIALOG_MSG_ADDED_" + sender.name()).toString(), msg, sender.name().toLowerCase());
			}catch (NumberFormatException ex){
				Lang.NUMBER_INVALID.send(p, args[1]);
			}
			break;
			
		case EDIT:
			if (args.length < 3){
				Lang.DIALOG_SYNTAX.send(p, cmd, " <id>");
				break;
			}
			try{
				Message message = d.messages.get(Integer.parseInt(args[1]));
				if (message == null) {
					Lang.OUT_OF_BOUNDS.send(p, args[1], 0, d.messages.size());
				}else {
					msg = Utils.buildFromArray(argsColored, 2, " ");
					message.text = msg;
					Lang.DIALOG_MSG_EDITED.send(p, msg);
				}
			}catch (IllegalArgumentException ex){
				Utils.sendMessage(p, Lang.NUMBER_INVALID.toString());
			}
			break;
			
		case ADDSOUND:
			if (args.length < 3){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + "addSound <id> <sound>");
				break;
			}
			try{
				Message imsg = d.messages.get(Integer.parseInt(args[1]));
				if (imsg == null){
					Lang.OBJECT_DOESNT_EXIST.send(p, args[1]);
					break;
				}
				Lang.DIALOG_SOUND_ADDED.send(p, imsg.sound = args[2], args[1]);
			}catch (IllegalArgumentException ex){
				Utils.sendMessage(p, Lang.NUMBER_INVALID.toString());
			}
			break;
			
		case SETTIME:
			if (args.length < 3) {
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + "setTime <id> <time in ticks>");
				break;
			}
			try {
				Message imsg = d.messages.get(Integer.parseInt(args[1]));
				if (imsg == null) {
					Lang.OBJECT_DOESNT_EXIST.send(p, args[1]);
					break;
				}
				int time = Integer.parseInt(args[2]);
				if (time < 0) {
					imsg.wait = -1;
					Lang.DIALOG_TIME_REMOVED.send(p, args[1]);
				}else {
					imsg.wait = time;
					Lang.DIALOG_TIME_SET.send(p, args[1], imsg.wait = time);
				}
			}catch (IllegalArgumentException ex) {
				Utils.sendMessage(p, Lang.NUMBER_INVALID.toString());
			}
			break;
		
		case NPCNAME:
			if (args.length < 2) {
				Lang.DIALOG_NPCNAME_UNSET.send(p, d.npcName);
				d.npcName = null;
			}
			Lang.DIALOG_NPCNAME_SET.send(p, d.npcName, d.npcName = msg);
			break;

		case CLEAR:
			Lang.DIALOG_CLEARED.send(p, d.messages.size());
			d.messages.clear();
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.dialog.help.")) l.sendWP(p);
			}
			break;

		case CLOSE:
			leave(p);
			end.run();
			break;

		}
		return true;
	}

	private enum Command{
		NPC, PLAYER, NOSENDER, REMOVE, LIST, HELP, CLOSE, NPCINSERT, PLAYERINSERT, NOSENDERINSERT, EDIT, ADDSOUND, SETTIME, NPCNAME, CLEAR;
	}
	
}
