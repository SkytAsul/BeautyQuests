package fr.skytasul.quests.api.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.creation.RewardsGUI;

public interface RewardCreationRunnables {

	/**
	 * Called when item clicked
	 * @param p player who click on the item
	 * @param datas datas of the reward
	 * @param gui RewardsGui opened for the player
	 * @param clicked ItemStack clicked
	 */
	public abstract void itemClick(Player p, Map<String, Object> datas, RewardsGUI gui, ItemStack clicked);

	/**
	 * Called when quest edition started
	 * @param datas all datas saved when stage creation
	 * @param reward Existing reward to edit
	 * @param item itemStack associated
	 * 
	 */
	public abstract void edit(Map<String, Object> datas, AbstractReward reward, ItemStack item);

	/**
	 * Called when quest creation finished
	 * @param datas all datas saved when stage creation
	 * @return AbstractRequirement created
	 */

	public abstract AbstractReward finish(Map<String, Object> datas);
	
}
