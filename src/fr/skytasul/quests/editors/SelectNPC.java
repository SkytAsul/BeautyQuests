package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.RunnableObj;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class SelectNPC extends InventoryClear{
	
	private RunnableObj run;
	
	public SelectNPC(Player p, RunnableObj end){
		super(p);
		this.run = end;
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onNPCClick(NPCRightClickEvent e){
		if (e.getClicker() != p) return;
		e.setCancelled(true);
		leave(e.getClicker());
		run.run(e.getNPC());
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
	public void onTchat(AsyncPlayerChatEvent e){
		if (e.getPlayer() != p) return;
		if (e.getMessage().equals("cancel")){
			e.setCancelled(true);
			leave(e.getPlayer());
			run.run(null);
		}
	}

	public void begin(){
		super.begin();
		Utils.sendMessage(p, Lang.NPC_EDITOR_ENTER.toString());
	}
	
	public void end(){
		super.end();
	}

}