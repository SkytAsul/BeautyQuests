package fr.skytasul.quests.api.editors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryClear extends Editor{
	
	private ItemStack[] contents = new ItemStack[0];
	private int heldItemSlot;

	public InventoryClear(Player p, Runnable cancel) {
		super(p, cancel);
	}

	@Override
	public void begin(){
		super.begin();
		contents = player.getInventory().getContents();
		heldItemSlot = player.getInventory().getHeldItemSlot();
		player.getInventory().setContents(new ItemStack[0]);
	}

	@Override
	public void end(){
		super.end();
		player.getInventory().setContents(contents);
		player.getInventory().setHeldItemSlot(heldItemSlot);
	}
	
	@Override
	protected String cancelWord() {
		return "cancel";
	}
	
}
