package fr.skytasul.quests.api.stages;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

public class StageCreator {
	public static final LinkedList<StageCreator> creators = new LinkedList<>();
	
	public final StageType type;
	public final ItemStack item;
	public final StageCreationRunnables runnables;
	
	public StageCreator(StageType type, ItemStack is, StageCreationRunnables runnables){
		this.type = type;
		this.item = is;
		this.runnables = runnables;
	}
	
	public static StageCreator getCreator(StageType type){
		for (StageCreator creator : creators){
			if (creator.type == type) return creator;
		}
		return null;
	}
	
}
