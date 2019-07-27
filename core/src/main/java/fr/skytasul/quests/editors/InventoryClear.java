package fr.skytasul.quests.editors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryClear extends Editor{
	
	private ItemStack[] contents = new ItemStack[0];

	public InventoryClear(Player p){
		super(p);
	}

	public void begin(){
		super.begin();
		contents = p.getInventory().getContents();
		p.getInventory().setContents(new ItemStack[0]);
	}

	public void end(){
		p.getInventory().setContents(contents);
	}
	
}
