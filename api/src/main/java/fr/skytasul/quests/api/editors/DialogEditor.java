package fr.skytasul.quests.api.editors;

import org.bukkit.entity.Player;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.api.npcs.dialogs.Message.Sender;
import fr.skytasul.quests.api.utils.MessageUtils;
import fr.skytasul.quests.api.utils.Utils;

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
			Lang.COMMAND_DOESNT_EXIST_NOSLASH.send(player);
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
				Lang.DIALOG_SYNTAX.send(player, cmd, "");
				break;
			}
			d.add(msg, Sender.valueOf(cmd.name()));
			MessageUtils.sendPrefixedMessage(player, Lang.valueOf("DIALOG_MSG_ADDED_" + cmd.name()).toString(), msg,
					cmd.name().toLowerCase());
			break;

		case REMOVE:
			if (!hasMsg){
				Lang.DIALOG_REMOVE_SYNTAX.send(player);
				break;
			}
			try{
				Message removed = d.getMessages().remove(Integer.parseInt(args[1]));
				if (removed != null){
					Lang.DIALOG_MSG_REMOVED.send(player, removed.text);
				} else
					Lang.OUT_OF_BOUNDS.send(player, args[1], 0, d.getMessages().size());
			}catch (IllegalArgumentException ex){
				Lang.NUMBER_INVALID.send(player);
			}
			break;

		case LIST:
			for (int i = 0; i < d.getMessages().size(); i++) {
				Message dmsg = d.getMessages().get(i);
				MessageUtils.sendRawMessage(player, "§6{0}: §7\"{1}§7\"§e by §l{2}", false, i, dmsg.text,
						dmsg.sender.name().toLowerCase());
			}
			break;
			
		case NOSENDERINSERT:
		case NPCINSERT:
		case PLAYERINSERT:
			if (args.length < 3){
				Lang.DIALOG_SYNTAX.send(player, cmd, " <id>");
				break;
			}
			try{
				msg = Utils.buildFromArray(argsColored, 2, " ");
				Sender sender = Sender.valueOf(cmd.name().replace("INSERT", ""));
				d.insert(msg, sender, Integer.parseInt(args[1]));
				MessageUtils.sendPrefixedMessage(player, Lang.valueOf("DIALOG_MSG_ADDED_" + sender.name()).toString(), msg,
						sender.name().toLowerCase());
			}catch (NumberFormatException ex){
				Lang.NUMBER_INVALID.send(player, args[1]);
			}
			break;
			
		case EDIT:
			if (args.length < 3){
				Lang.DIALOG_SYNTAX.send(player, cmd, " <id>");
				break;
			}
			try{
				Message message = d.getMessages().get(Integer.parseInt(args[1]));
				msg = Utils.buildFromArray(argsColored, 2, " ");
				message.text = msg;
				Lang.DIALOG_MSG_EDITED.send(player, msg);
			}catch (IllegalArgumentException ex){
				Lang.NUMBER_INVALID.send(player);
			}catch (IndexOutOfBoundsException ex) {
				Lang.OBJECT_DOESNT_EXIST.send(player, args[1]);
			}
			break;
			
		case ADDSOUND:
			if (args.length < 3){
				Lang.TEXTLIST_SYNTAX.send(player, "addSound <id> <sound>");
				break;
			}
			try{
				Message imsg = d.getMessages().get(Integer.parseInt(args[1]));
				Lang.DIALOG_SOUND_ADDED.send(player, imsg.sound = args[2], args[1]);
			}catch (IllegalArgumentException ex){
				Lang.NUMBER_INVALID.send(player);
			}catch (IndexOutOfBoundsException ex) {
				Lang.OBJECT_DOESNT_EXIST.send(player, args[1]);
			}
			break;
			
		case SETTIME:
			if (args.length < 3) {
				Lang.TEXTLIST_SYNTAX.send(player, "setTime <id> <time in ticks>");
				break;
			}
			try {
				Message imsg = d.getMessages().get(Integer.parseInt(args[1]));
				int time = Integer.parseInt(args[2]);
				if (time < 0) {
					imsg.wait = -1;
					Lang.DIALOG_TIME_REMOVED.send(player, args[1]);
				}else {
					imsg.wait = time;
					Lang.DIALOG_TIME_SET.send(player, args[1], imsg.wait = time);
				}
			}catch (IllegalArgumentException ex) {
				Lang.NUMBER_INVALID.send(player);
			}catch (IndexOutOfBoundsException ex) {
				Lang.OBJECT_DOESNT_EXIST.send(player, args[1]);
			}
			break;
		
		case NPCNAME:
			if (args.length < 2) {
				Lang.DIALOG_NPCNAME_UNSET.send(player, d.getNpcName());
				d.setNpcName(null);
			} else {
				String oldName = d.getNpcName();
				d.setNpcName(msg);
				Lang.DIALOG_NPCNAME_SET.send(player, oldName, msg);
			}
			break;
		
		case SKIPPABLE:
			String prev = d.getSkippableStatus();
			if (args.length < 2) {
				d.setSkippable(null);;
				Lang.DIALOG_SKIPPABLE_UNSET.send(player, prev);
			}else {
				d.setSkippable(Boolean.parseBoolean(args[1]));
				Lang.DIALOG_SKIPPABLE_SET.send(player, prev, d.getSkippableStatus());
			}
			break;

		case CLEAR:
			Lang.DIALOG_CLEARED.send(player, d.getMessages().size());
			d.getMessages().clear();
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.dialog.help.")) l.sendWP(player);
			}
			break;

		case CLOSE:
			stop();
			end.run();
			break;

		}
		return true;
	}

	private enum Command{
		NPC, PLAYER, NOSENDER, REMOVE, LIST, HELP, CLOSE, NPCINSERT, PLAYERINSERT, NOSENDERINSERT, EDIT, ADDSOUND, SETTIME, NPCNAME, SKIPPABLE, CLEAR;
	}
	
}
