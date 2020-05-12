package fr.skytasul.quests.editors;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class SelectNPC extends InventoryClear{
	
	private Consumer<NPC> run;
	
	public SelectNPC(Player p, Consumer<NPC> end){
		super(p);
		this.run = end;
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onNPCClick(NPCRightClickEvent e){
		if (e.getClicker() != p) return;
		e.setCancelled(true);
		leave(e.getClicker());
		run.accept(e.getNPC());
	}
	
	public boolean chat(String msg, String strippedMessage){
		if (strippedMessage.equals("cancel")) {
			leave(p);
			run.accept(null);
			return true;
		}
		return false;
	}

	protected String cancelWord(){
		return "cancel";
	}
	
	public void begin(){
		super.begin();
		Utils.sendMessage(p, Lang.NPC_EDITOR_ENTER.toString());
	}
	
	public void end(){
		super.end();
	}

}