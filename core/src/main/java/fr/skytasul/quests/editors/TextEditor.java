package fr.skytasul.quests.editors;

import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.skytasul.quests.editors.checkers.AbstractParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class TextEditor extends Editor{
	
	protected Consumer<Object> run;
	public Runnable cancel;
	public Runnable nul;
	
	public AbstractParser parser;
	
	public TextEditor(Player p, Consumer<Object> end){
		this(p, end, null, null, null);
	}
	
	public TextEditor(Player p, Consumer<Object> end, Runnable cancel, Runnable nul){
		this(p, end, null, cancel, nul);
	}
	
	public TextEditor(Player p, Consumer<Object> end, AbstractParser parser){
		this(p, end, parser, null, null);
	}
	
	public TextEditor(Player p, Consumer<Object> end, AbstractParser parser, Runnable cancel, Runnable nul){
		super(p);
		this.run = end;
		this.parser = parser;
		this.cancel = cancel;
		this.nul = nul;
	}

	public boolean chat(String msg){
		if (msg.equals("cancel")){
			if (cancel == null){
				Utils.sendMessage(p, Lang.ARG_NOT_SUPPORTED.toString(), "cancel");
				return false;
			}
			leave(p);
			cancel.run();
			return true;
		}else if (msg.equals("null")){
			if (nul == null){
				Utils.sendMessage(p, Lang.ARG_NOT_SUPPORTED.toString(), "null");
				return false;
			}
			leave(p);
			nul.run();
			return true;
		}
		
		Object returnment = msg;
		boolean invalid = false;
		if (parser != null){
			try{
				Object tmp = parser.parse(p, ChatColor.stripColor(msg));
				if (tmp == null){
					invalid = true;
				}else {
					returnment = tmp;
				}
			}catch (Throwable ex){
				Lang.ERROR_OCCURED.send(p, msg + " parsingText");
				invalid = true;
				ex.printStackTrace();
			}
		}

		if (!invalid){
			leave(p);
			run.accept(returnment);
			return true;
		}
		return false;
	}
	
}
