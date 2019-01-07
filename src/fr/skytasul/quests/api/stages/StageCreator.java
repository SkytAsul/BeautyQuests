package fr.skytasul.quests.api.stages;

import java.util.LinkedHashMap;

import org.bukkit.inventory.ItemStack;

public class StageCreator {
	public static final LinkedHashMap<StageType, StageCreator> creators = new LinkedHashMap<>();
	
	public final ItemStack item;
	public final StageCreationRunnables runnables;
	/*public final StageEditRunnable launchEdit;
	public final StageCraftRunnable finish;*/
	
	public StageCreator(ItemStack is, StageCreationRunnables runnables/*, StageEditRunnable launchEdit, StageCraftRunnable finish*/){
		this.item = is;
		this.runnables = runnables;
		/*this.launchEdit = launchEdit;
		this.finish = finish;*/
	}
	
	public static LinkedHashMap<StageType, StageCreator> getCreators(){
		return creators;
	}
	
}
