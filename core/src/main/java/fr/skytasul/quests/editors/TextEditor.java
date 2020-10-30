package fr.skytasul.quests.editors;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

import fr.skytasul.quests.editors.checkers.AbstractParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class TextEditor<T> extends Editor {
	
	protected Consumer<T> run;
	public Runnable nul;
	
	public AbstractParser<T> parser;
	
	public TextEditor(Player p, Runnable cancel, Consumer<T> end) {
		this(p, cancel, end, null, null);
	}
	
	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable nul) {
		this(p, cancel, end, nul, null);
	}
	
	public TextEditor(Player p, Runnable cancel, Consumer<T> end, AbstractParser<T> parser) {
		this(p, cancel, end, null, parser);
	}
	
	public TextEditor(Player p, Runnable cancel, Consumer<T> end, Runnable nul, AbstractParser<T> parser) {
		super(p, cancel);
		this.run = end;
		this.parser = parser;
		this.nul = nul;
	}

	public boolean chat(String msg, String strippedMessage){
		if (strippedMessage.equals("null")) {
			if (nul == null){
				Utils.sendMessage(p, Lang.ARG_NOT_SUPPORTED.toString(), "null");
				return false;
			}
			leave(p);
			nul.run();
			return true;
		}
		
		T returnment = null;
		boolean invalid = false;
		if (parser != null){
			try{
				T tmp = parser.parse(p, strippedMessage);
				if (tmp == null){
					invalid = true;
				}else {
					returnment = tmp;
				}
			}catch (Throwable ex){
				Lang.ERROR_OCCURED.send(p, strippedMessage + " parsingText");
				invalid = true;
				ex.printStackTrace();
			}
		}else returnment = (T) strippedMessage;

		if (!invalid){
			leave(p);
			run.accept(returnment);
			return true;
		}
		return false;
	}
	
}
