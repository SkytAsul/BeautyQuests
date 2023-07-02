package fr.skytasul.quests.api.editors;

import java.util.function.Consumer;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import fr.skytasul.quests.api.editors.parsers.AbstractParser;
import fr.skytasul.quests.api.localization.Lang;

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
				Lang.ARG_NOT_SUPPORTED.send(player, "null");
				return false;
			}
			stop();
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
				T tmp = parser.parse(player, strippedMessage);
				if (tmp == null){
					invalid = true;
				}else {
					returnment = tmp;
				}
			}catch (Throwable ex){
				Lang.ERROR_OCCURED.send(player, strippedMessage + " parsingText");
				invalid = true;
				ex.printStackTrace();
			}
		}else returnment = (T) (useStripped ? strippedMessage : msg);

		if (!invalid){
			stop();
			run.accept(returnment);
			return true;
		}
		return false;
	}
	
	@Override
	public void begin() {
		super.begin();
		if (parser != null) parser.sendIndication(player);
	}
	
}
