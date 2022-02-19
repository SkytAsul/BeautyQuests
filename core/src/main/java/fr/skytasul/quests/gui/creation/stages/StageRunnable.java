package fr.skytasul.quests.gui.creation.stages;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface StageRunnable {

	/**
	 * Called when the item is clicked
	 * @param p player who click on the item
	 * @param item item clicked
	 */
	public abstract void run(Player p, ItemStack item);
	
	public interface StageRunnableClick extends StageRunnable {
		
		@Override
		default void run(Player p, ItemStack item) {
			run(p, item, ClickType.RIGHT);
		}
		
		public abstract void run(Player p, ItemStack item, ClickType click);
		
	}
	
}
