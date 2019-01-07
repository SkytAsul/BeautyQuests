package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.skytasul.quests.editors.checkers.AbstractParser;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;

public class TextEditor extends Editor{
	
	protected RunnableObj run;
	public Runnable cancel;
	public Runnable nul;
	
	public AbstractParser parser;
	
	public TextEditor(Player p, RunnableObj end){
		this(p, end, null, null, null);
	}
	
	public TextEditor(Player p, RunnableObj end, Runnable cancel, Runnable nul){
		this(p, end, null, cancel, nul);
	}
	
	public TextEditor(Player p, RunnableObj end, AbstractParser parser){
		this(p, end, parser, null, null);
	}
	
	public TextEditor(Player p, RunnableObj end, AbstractParser parser, Runnable cancel, Runnable nul){
		super(p);
		this.run = end;
		this.parser = parser;
		this.cancel = cancel;
		this.nul = nul;
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onTchat(AsyncPlayerChatEvent e){
		if (p != e.getPlayer()) return;
		e.setMessage(e.getMessage().replaceAll("&", "§"));
		e.setCancelled(true);
		
		if (e.getMessage().equals("cancel")){
			if (cancel == null){
				Utils.sendMessage(p, Lang.ARG_NOT_SUPPORTED.toString(), "cancel");
			}else {
				leave(e.getPlayer());
				cancel.run();
			}
			return;
		}else if (e.getMessage().equals("null")){
			if (nul == null){
				Utils.sendMessage(p, Lang.ARG_NOT_SUPPORTED.toString(), "null");
			}else {
				leave(e.getPlayer());
				nul.run();
			}
			return;
		}
		
		Object returnment = e.getMessage();
		boolean invalid = false;
		/*if (integer){
			try{
				int i = Integer.parseInt(e.getMessage());
				if (positive && i < 0){
					Lang.NUMBER_NEGATIVE.send(p);
					invalid = true;
				}else {
					returnment = i;
				}
			}catch (NumberFormatException ex){
				Lang.NUMBER_INVALID.send(p, e.getMessage());
				invalid = true;
			}
		}*/
		if (parser != null){
			try{
				Object tmp = parser.parse(p, e.getMessage());
				if (tmp == null){
					invalid = true;
				}else {
					returnment = tmp;
				}
			}catch (Throwable ex){
				Lang.ERROR_OCCURED.send(p, e.getMessage() + " parsingText");
				invalid = true;
				ex.printStackTrace();
			}
		}

		if (!invalid){
			leave(e.getPlayer());
			run.run(returnment);
		}
	}
	
}
