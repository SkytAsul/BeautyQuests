package fr.skytasul.quests.editors;

import java.util.Map.Entry;

import org.bukkit.entity.Player;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Dialog;
import fr.skytasul.quests.utils.types.Message;
import fr.skytasul.quests.utils.types.Message.Sender;
import fr.skytasul.quests.utils.types.RunnableObj;
import net.citizensnpcs.api.npc.NPC;

public class DialogEditor extends Editor{
	
	private RunnableObj run;
	public Dialog d;

	public DialogEditor(Player p, NPC npc, RunnableObj run, Dialog dialog){
		super(p);
		this.run = run;
		this.d = dialog;
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
		
		case NOSENDER:
		case NPC:
		case PLAYER:
			if (!hasMsg){
				Lang.DIALOG_SYNTAX.send(p, cmd, "");
				break;
			}
			d.add(msg, Sender.valueOf(comd.name()));
			Utils.sendMessage(p, Lang.valueOf("DIALOG_MSG_ADDED_" + comd.name()).toString(), msg, comd.name().toLowerCase());
			break;

		case REMOVE:
			if (!hasMsg){
				Lang.DIALOG_REMOVE_SYNTAX.send(p);
				break;
			}
			try{
				Message removed = d.messages.remove(Integer.parseInt(args[1]), true);
				if (removed != null){
					Utils.sendMessage(p, Lang.DIALOG_MSG_REMOVED.toString(), removed.text);
				}else Lang.OUT_OF_BOUNDS.send(p, args[1], 0, d.messages.valuesSize());
			}catch (IllegalArgumentException ex){
				Utils.sendMessage(p, Lang.NUMBER_INVALID.toString());
			}
			break;

		case LIST:
			for (Entry<Integer, Message> en : d.messages.getOriginalMap().entrySet()){
				p.sendMessage("§6" + en.getKey() + " :§a \"" + en.getValue().text + "§r§a\"§e by §l" + en.getValue().sender.name().toLowerCase());
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
				msg = Utils.buildFromArray(args, 2, " ");
				Sender sender = Sender.valueOf(comd.name().replace("INSERT", ""));
				d.insert(msg, sender, Integer.parseInt(args[1]));
				Utils.sendMessage(p, Lang.valueOf("DIALOG_MSG_ADDED_" + sender.name()).toString(), msg, sender.name().toLowerCase());
			}catch (NumberFormatException ex){
				Lang.NUMBER_INVALID.send(p, args[1]);
			}
			break;
			
		case ADDSOUND:
			if (args.length < 3){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + "addsound <id> <sound>");
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
			
		case CLEAR:
			Lang.DIALOG_CLEARED.send(p, d.messages.clear());
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.dialog.help.")) l.sendWP(p);
			}
			break;

		case CLOSE:
			leave(p);
			run.run(d);
			break;

		}
		return true;
	}

	private enum Command{
		NPC, PLAYER, NOSENDER, REMOVE, LIST, HELP, CLOSE, NPCINSERT, PLAYERINSERT, NOSENDERINSERT, ADDSOUND, CLEAR;
	}
	
}
