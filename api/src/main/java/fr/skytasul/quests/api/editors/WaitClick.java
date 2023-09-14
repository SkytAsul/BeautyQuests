package fr.skytasul.quests.api.editors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.gui.ItemUtils;

public class WaitClick extends InventoryClear implements Listener {

	private ItemStack validateItem;
	private ItemStack noneItem;
	private Runnable validate;
	private Runnable none;

	public WaitClick(Player p, Runnable cancel, ItemStack validateItem, Runnable validate) {
		this(p, cancel, validateItem, validate, null, null);
	}

	public WaitClick(Player p, Runnable cancel, ItemStack validateItem, Runnable validate, ItemStack noneItem, Runnable none) {
		super(p, cancel);
		this.validateItem = validateItem;
		this.noneItem = noneItem;
		this.validate = validate;
		this.none = none;
	}

	@EventHandler (priority = EventPriority.LOW)
	public void onInteract(PlayerInteractEvent e) {
		if (e.getPlayer() != player) return;
		if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (e.getItem() == null) return;
		Runnable run;
		if (e.getItem().equals(validateItem)){
			run = validate;
		}else if (e.getItem().equals(noneItem)){
			run = none;
		}else if (ItemUtils.itemCancel.equals(e.getItem())) {
			run = cancel;
		}else return;
		e.setCancelled(true);
		stop();
		run.run();
	}

	@Override
	public void begin(){
		super.begin();
		if (none == null){
			player.getInventory().setItem(4, validateItem);
			player.getInventory().setHeldItemSlot(4);
		}else {
			player.getInventory().setItem(3, validateItem);
			player.getInventory().setHeldItemSlot(3);
			player.getInventory().setItem(5, noneItem);
		}
		if (cancel != null) player.getInventory().setItem(8, ItemUtils.itemCancel);
	}

}
