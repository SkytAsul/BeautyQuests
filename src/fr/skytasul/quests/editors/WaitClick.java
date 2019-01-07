package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WaitClick extends InventoryClear{
	
	private Runnable run;
	private ItemStack item;
	
	public WaitClick(Player p, Runnable end, ItemStack is){
		super(p);
		this.run = end;
		this.item = is;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getItem() == null) return;
		if (!e.getItem().equals(item)) return;
		e.setCancelled(true);
		leave(e.getPlayer());
		run.run();
	}

	public void begin(){
		super.begin();
		p.getInventory().setItem(4, item);
	}
	
	public void end(){
		super.end();
	}
	
}
