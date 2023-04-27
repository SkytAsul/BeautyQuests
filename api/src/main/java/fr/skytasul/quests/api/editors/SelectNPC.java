package fr.skytasul.quests.api.editors;

import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import fr.skytasul.quests.api.events.internal.BQNPCClickEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BQNPC;

public class SelectNPC extends InventoryClear{
	
	private Consumer<BQNPC> run;
	
	public SelectNPC(Player p, Runnable cancel, Consumer<BQNPC> end) {
		super(p, cancel);
		this.run = end;
	}
	
	@EventHandler (priority = EventPriority.LOW)
	private void onNPCClick(BQNPCClickEvent e) {
		if (e.getPlayer() != player) return;
		e.setCancelled(true);
		stop();
		run.accept(e.getNPC());
	}
	
	@Override
	public void begin(){
		super.begin();
		Lang.NPC_EDITOR_ENTER.send(player);
	}

}