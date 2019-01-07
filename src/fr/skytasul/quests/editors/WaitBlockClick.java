package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.utils.types.RunnableObj;

public class WaitBlockClick extends InventoryClear{
	
	private RunnableObj run;
	private ItemStack item;
	
	public WaitBlockClick(Player p, RunnableObj end, ItemStack is){
		super(p);
		this.run = end;
		this.item = is;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getClickedBlock() == null) return;
		if (e.getItem() == null) return;
		if (!e.getItem().equals(item)) return;
		e.setCancelled(true);
		leave(e.getPlayer());
		run.run(e.getClickedBlock().getLocation());
	}

	public void begin(){
		super.begin();
		p.getInventory().setItem(4, item);
	}
	
	public void end(){
		super.end();
	}
	
}
