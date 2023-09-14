package fr.skytasul.quests.api.editors;

import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.gui.ItemUtils;

public class WaitBlockClick extends InventoryClear implements Listener {

	private Consumer<Location> run;
	private ItemStack item;

	public WaitBlockClick(Player p, Runnable cancel, Consumer<Location> end, ItemStack is) {
		super(p, cancel);
		this.run = end;
		this.item = is;
	}

	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if (e.getPlayer() != player) return;
		if (ItemUtils.itemCancel.equals(e.getItem())) {
			cancel();
			return;
		}
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getClickedBlock() == null) return;
		if (!item.equals(e.getItem())) return;
		e.setCancelled(true);
		stop();
		run.accept(e.getClickedBlock().getLocation());
	}

	@Override
	public void begin(){
		super.begin();
		player.getInventory().setItem(4, item);
		player.getInventory().setHeldItemSlot(4);
		if (cancel != null) player.getInventory().setItem(8, ItemUtils.itemCancel);
	}

}
