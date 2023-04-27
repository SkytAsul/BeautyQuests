package fr.skytasul.quests.api.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import fr.skytasul.quests.api.gui.ItemUtils;

public abstract class CancellableEditor extends InventoryClear {
	
	public CancellableEditor(Player p, Runnable cancel) {
		super(p, cancel);
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onClick(PlayerInteractEvent e) {
		if (e.getPlayer() != player) return;
		if (ItemUtils.itemCancel.equals(e.getItem())) {
			e.setCancelled(true);
			cancel();
		}
	}
	
	@Override
	public void begin() {
		super.begin();
		if (cancel != null) player.getInventory().setItem(8, ItemUtils.itemCancel);
	}
	
}
