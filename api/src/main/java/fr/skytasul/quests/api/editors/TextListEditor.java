package fr.skytasul.quests.api.editors;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class TextListEditor extends Editor{
	
	protected Consumer<List<String>> run;
	public Predicate<String> valid;
	
	private List<String> texts;
	
	public TextListEditor(Player p, Consumer<List<String>> end, List<String> texts){
		super(p, null);
		Validate.notNull(texts, "Text list in Editor cannot be null.");
		this.run = end;
		this.texts = texts;
	}
	
	@Override
	public void begin() {
		super.begin();
		Lang.ENTER_EDITOR_LIST.send(player);
	}

	@Override
	public boolean chat(String coloredMessage, String strippedMessage){
		String[] args = strippedMessage.split(" ");
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
			msg = Utils.buildFromArray(coloredMessage.split(" "), 1, " ");
			hasMsg = true;
		}
		switch (cmd) {
		
		case ADD:
			if (!hasMsg){
				sendSyntax(" add <message>");
				break;
			}
			if (valid != null){
				if (!valid.test(msg)) break;
			}
			texts.add(msg);
			Lang.TEXTLIST_TEXT_ADDED.send(player, PlaceholderRegistry.of("msg", msg));
			break;

		case REMOVE:
			if (!hasMsg){
				sendSyntax(" remove <id>");
				break;
			}
			try{
				String removed = texts.remove(Integer.parseInt(args[1]));
				Lang.TEXTLIST_TEXT_REMOVED.send(player, PlaceholderRegistry.of("msg", removed));
			}catch (IllegalArgumentException ex){
				Lang.NUMBER_INVALID.send(player);
			}
			break;

		case LIST:
			StringJoiner joiner = new StringJoiner("\n", "§6§lList:\n", "");
			for (int i = 0; i < texts.size(); i++) {
				joiner.add("§6" + i + ": §r" + texts.get(i));
			}
			player.sendMessage(joiner.toString());
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.textList.help."))
					l.send(player);
			}
			break;

		case CLOSE:
			stop();
			run.accept(texts);
			break;

		}
		return true;
	}

	private void sendSyntax(String command) {
		Lang.TEXTLIST_SYNTAX.send(player, PlaceholderRegistry.of("command", command));
	}

	private enum Command{
		ADD, REMOVE, LIST, HELP, CLOSE;
	}
	
}
