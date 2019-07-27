package fr.skytasul.quests.api.requirements;

import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.gui.creation.RequirementsGUI;

public interface RequirementCreationRunnables {

	/**
	 * Called when item clicked
	 * @param p player who click on the item
	 * @param datas datas of the requirement
	 * @param gui RequirementsGui opened for the player
	 */
	public abstract void itemClick(Player p, Map<String, Object> datas, RequirementsGUI gui);

	/**
	 * Called when quest edition started
	 * @param datas all datas saved when stage creation
	 * @param requirement Existing requirement
	 */
	public abstract void edit(Map<String, Object> datas, AbstractRequirement requirement);

	/**
	 * Called when quest creation finished
	 * @param datas all datas saved when stage creation
	 * @return AbstractRequirement created
	 */

	public abstract AbstractRequirement finish(Map<String, Object> datas);
	
}
