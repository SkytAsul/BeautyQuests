package fr.skytasul.quests.gui.creation.stages;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface StageRunnable {

	/**
	 * Called when item clicked
	 * @param p player who click on the item
	 * @param datas datas of the line
	 * @param item item clicked
	 */
	public abstract void run(Player p, LineData datas, ItemStack item);
	
}
