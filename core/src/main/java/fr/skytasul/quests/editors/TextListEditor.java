package fr.skytasul.quests.editors;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

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
	protected void begin() {
		super.begin();
		Lang.ENTER_EDITOR_LIST.send(p);
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
			Utils.sendMessage(p, Lang.COMMAND_DOESNT_EXIST_NOSLASH.toString());
			return false;
		}
		if (args.length > 1){
			msg = Utils.buildFromArray(coloredMessage.split(" "), 1, " ");
			hasMsg = true;
		}
		switch (cmd) {
		
		case ADD:
			if (!hasMsg){
				Utils.sendMessage(p, Lang.TEXTLIST_SYNTAX.toString() + " add <message>");
				break;
			}
			if (valid != null){
				if (!valid.test(msg)) break;
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
			StringJoiner joiner = new StringJoiner("\n", "§6§lList:\n", "");
			for (int i = 0; i < texts.size(); i++) {
				joiner.add("§6" + i + ": §r" + texts.get(i));
			}
			p.sendMessage(joiner.toString());
			break;

		case HELP:
			for (Lang l : Lang.values()){
				if (l.getPath().startsWith("msg.editor.textList.help.")) l.sendWP(p);
			}
			break;

		case CLOSE:
			leave(p);
			run.accept(texts);
			break;

		}
		return true;
	}

	private enum Command{
		ADD, REMOVE, LIST, HELP, CLOSE;
	}
	
}
