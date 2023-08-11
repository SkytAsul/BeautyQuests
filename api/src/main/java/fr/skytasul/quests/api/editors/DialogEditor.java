package fr.skytasul.quests.api.editors;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.api.npcs.dialogs.Message.Sender;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;

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
			msg = Arrays.stream(argsColored).skip(1).collect(Collectors.joining(" "));
			hasMsg = true;
		}
		switch (cmd) {
		
		case NOSENDER:
		case NPC:
		case PLAYER:
			if (!hasMsg){
				Lang.DIALOG_MESSAGE_SYNTAX.send(player, PlaceholderRegistry.of("command", cmd));
				break;
			}
			d.add(msg, Sender.valueOf(cmd.name()));
			Lang.valueOf("DIALOG_MSG_ADDED_" + cmd.name()).send(player, PlaceholderRegistry.of("msg", msg));
			break;

		case REMOVE:
			if (!hasMsg){
				Lang.DIALOG_REMOVE_SYNTAX.send(player);
				break;
			}
			try{
				int index = Integer.parseInt(args[1]);
				Message removed = d.getMessages().remove(index);
				if (removed != null){
					Lang.DIALOG_MSG_REMOVED.send(player, PlaceholderRegistry.of("msg", removed.text));
				} else
					DefaultErrors.sendOutOfBounds(player, index, 0, d.getMessages().size());
			}catch (IllegalArgumentException ex){
				DefaultErrors.sendInvalidNumber(player, args[1]);
			}
			break;

		case LIST:
			for (int i = 0; i < d.getMessages().size(); i++) {
				Message dmsg = d.getMessages().get(i);
				MessageUtils.sendRawMessage(player, "§6{index}: §7\"{msg}§7\"§e by §l{sender}",
						PlaceholderRegistry.of("index", i, "msg", dmsg.text, "sender", dmsg.sender.name().toLowerCase()),
						PlaceholdersContext.DEFAULT_CONTEXT);
			}
			break;
			
		case NOSENDERINSERT:
		case NPCINSERT:
		case PLAYERINSERT:
			if (args.length < 3){
				Lang.DIALOG_MESSAGE_SYNTAX.send(player, PlaceholderRegistry.of("command", cmd + " <id>"));
				break;
			}
			try{
				msg = Arrays.stream(argsColored).skip(2).collect(Collectors.joining(" "));
				Sender sender = Sender.valueOf(cmd.name().replace("INSERT", ""));
				d.insert(msg, sender, Integer.parseInt(args[1]));
				Lang.valueOf("DIALOG_MSG_ADDED_" + cmd.name()).send(player, PlaceholderRegistry.of("msg", msg));
			}catch (NumberFormatException ex){
				DefaultErrors.sendInvalidNumber(player, args[1]);
			}
			break;
			
		case EDIT:
			if (args.length < 3){
				Lang.DIALOG_MESSAGE_SYNTAX.send(player, PlaceholderRegistry.of("command", cmd + " <id>"));
				break;
			}
			try{
				Message message = d.getMessages().get(Integer.parseInt(args[1]));
				msg = Arrays.stream(argsColored).skip(2).collect(Collectors.joining(" "));
				message.text = msg;
				Lang.DIALOG_MSG_EDITED.send(player, PlaceholderRegistry.of("msg", msg));
			}catch (IllegalArgumentException ex){
				DefaultErrors.sendInvalidNumber(player, args[1]);
			}catch (IndexOutOfBoundsException ex) {
				DefaultErrors.sendOutOfBounds(player, Integer.parseInt(args[1]), 0, d.getMessages().size());
			}
			break;
			
		case ADDSOUND:
			if (args.length < 3){
				Lang.TEXTLIST_SYNTAX.send(player, PlaceholderRegistry.of("command", "addSound <id> <sound>"));
				break;
			}
			try{
				Message imsg = d.getMessages().get(Integer.parseInt(args[1]));
				Lang.DIALOG_SOUND_ADDED.send(player, PlaceholderRegistry.of("sound", imsg.sound = args[2], "msg", args[1]));
			}catch (IllegalArgumentException ex){
				DefaultErrors.sendInvalidNumber(player, args[1]);
			}catch (IndexOutOfBoundsException ex) {
				DefaultErrors.sendOutOfBounds(player, Integer.parseInt(args[1]), 0, d.getMessages().size());
			}
			break;
			
		case SETTIME:
			if (args.length < 3) {
				Lang.TEXTLIST_SYNTAX.send(player, PlaceholderRegistry.of("command", "setTime <id> <time in ticks>"));
				break;
			}
			try {
				Message imsg = d.getMessages().get(Integer.parseInt(args[1]));
				int time = Integer.parseInt(args[2]);
				if (time < 0) {
					imsg.wait = -1;
					Lang.DIALOG_TIME_REMOVED.send(player, PlaceholderRegistry.of("msg", args[1]));
				}else {
					imsg.wait = time;
					Lang.DIALOG_TIME_SET.send(player, PlaceholderRegistry.of("msg", args[1], "time", imsg.wait = time));
				}
			}catch (IllegalArgumentException ex) {
				DefaultErrors.sendInvalidNumber(player, args[1]);
			}catch (IndexOutOfBoundsException ex) {
				DefaultErrors.sendOutOfBounds(player, Integer.parseInt(args[1]), 0, d.getMessages().size());
			}
			break;
		
		case NPCNAME:
			if (args.length < 2) {
				Lang.DIALOG_NPCNAME_UNSET.send(player, PlaceholderRegistry.of("old_name", d.getNpcName()));
				d.setNpcName(null);
			} else {
				String oldName = d.getNpcName();
				d.setNpcName(msg);
				Lang.DIALOG_NPCNAME_SET.send(player, PlaceholderRegistry.of("old_name", oldName, "new_name", msg));
			}
			break;
		
		case SKIPPABLE:
			String prev = d.getSkippableStatus();
			if (args.length < 2) {
				d.setSkippable(null);;
				Lang.DIALOG_SKIPPABLE_UNSET.send(player, PlaceholderRegistry.of("old_state", prev));
			}else {
				d.setSkippable(Boolean.parseBoolean(args[1]));
				Lang.DIALOG_SKIPPABLE_SET.send(player,
						PlaceholderRegistry.of("old_state", prev, "new_state", d.getSkippableStatus()));
			}
			break;

		case CLEAR:
			Lang.DIALOG_CLEARED.send(player, PlaceholderRegistry.of("amount", d.getMessages().size()));
			d.getMessages().clear();
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.dialog.help."))
					l.send(player);
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
