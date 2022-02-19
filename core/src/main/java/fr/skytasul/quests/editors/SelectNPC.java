package fr.skytasul.quests.editors;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import fr.skytasul.quests.api.events.BQNPCClickEvent;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class SelectNPC extends InventoryClear{
	
	private Consumer<BQNPC> run;
	
	public SelectNPC(Player p, Runnable cancel, Consumer<BQNPC> end) {
		super(p, cancel);
		this.run = end;
	}
	
	@EventHandler (priority = EventPriority.LOW)
	private void onNPCClick(BQNPCClickEvent e) {
		if (e.getPlayer() != p) return;
		e.setCancelled(true);
		leave(e.getPlayer());
		run.accept(e.getNPC());
	}
	
	@Override
	public void begin(){
		super.begin();
		Utils.sendMessage(p, Lang.NPC_EDITOR_ENTER.toString());
	}

}