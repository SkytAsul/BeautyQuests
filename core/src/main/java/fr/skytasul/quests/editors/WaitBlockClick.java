package fr.skytasul.quests.editors;

import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.gui.ItemUtils;

public class WaitBlockClick extends InventoryClear{
	
	private Consumer<Location> run;
	private ItemStack item;
	
	public WaitBlockClick(Player p, Runnable cancel, Consumer<Location> end, ItemStack is) {
		super(p, cancel);
		this.run = end;
		this.item = is;
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if (e.getPlayer() != p) return;
		if (ItemUtils.itemCancel.equals(e.getItem())) {
			cancel();
			return;
		}
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getClickedBlock() == null) return;
		if (!item.equals(e.getItem())) return;
		e.setCancelled(true);
		leave(e.getPlayer());
		run.accept(e.getClickedBlock().getLocation());
	}

	public void begin(){
		super.begin();
		p.getInventory().setItem(4, item);
		p.getInventory().setHeldItemSlot(4);
		if (cancel != null) p.getInventory().setItem(8, ItemUtils.itemCancel);
	}
	
}
