package fr.skytasul.quests.editors;

import java.util.function.Consumer;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import fr.skytasul.quests.editors.checkers.AbstractParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class TextEditor<T> extends Editor {
	
	protected Consumer<T> run;
	protected Runnable nul;
	protected AbstractParser<T> parser;
	protected boolean useStripped = false;
	protected boolean nullIntoConsumer = false;
	
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
	
	public TextEditor<T> passNullIntoEndConsumer() {
		Validate.isTrue(nul == null);
		nullIntoConsumer = true;
		return this;
	}
	
	public TextEditor<T> useStrippedMessage() {
		useStripped = true;
		return this;
	}

	@Override
	public boolean chat(String msg, String strippedMessage){
		if (strippedMessage.equals("null")) {
			if (nul == null && !nullIntoConsumer) {
				Utils.sendMessage(p, Lang.ARG_NOT_SUPPORTED.toString(), "null");
				return false;
			}
			leave(p);
			if (nullIntoConsumer) {
				run.accept(null);
			}else nul.run();
			return true;
		}else if (strippedMessage.equals("cancel") && cancel != null) {
			cancel();
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
		}else returnment = (T) (useStripped ? strippedMessage : msg);

		if (!invalid){
			leave(p);
			run.accept(returnment);
			return true;
		}
		return false;
	}
	
}
