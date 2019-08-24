package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WaitClick extends InventoryClear{
	
	private ItemStack validateItem;
	private ItemStack noneItem;
	private Runnable validate;
	private Runnable none;

	public WaitClick(Player p, ItemStack validateItem, Runnable validate){
		this(p, validateItem, validate, null, null);
	}
	
	public WaitClick(Player p, ItemStack validateItem, Runnable validate, ItemStack noneItem, Runnable none){
		super(p);
		this.validateItem = validateItem;
		this.validate = validate;
		this.none = none;
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onClick(PlayerInteractEvent e){
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getItem() == null) return;
		if (e.getItem().equals(validateItem)){
			validate.run();
		}else if (e.getItem().equals(noneItem)){
			none.run();
		}else return;
		e.setCancelled(true);
		leave(e.getPlayer());
	}

	public void begin(){
		super.begin();
		if (none == null){
			p.getInventory().setItem(4, validateItem);
		}else {
			p.getInventory().setItem(3, validateItem);
			p.getInventory().setItem(5, noneItem);
		}
	}
	
	public void end(){
		super.end();
	}
	
}
