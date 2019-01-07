package fr.skytasul.quests.api.requirements;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

public class RequirementCreator {
	public static final LinkedList<RequirementCreator> creators = new LinkedList<>();
	
	public final Class<? extends AbstractRequirement> clazz;
	public final ItemStack item;
	public final RequirementCreationRunnables runnables;
	
	public RequirementCreator(Class<? extends AbstractRequirement> clazz, ItemStack is, RequirementCreationRunnables runnables){
		this.clazz = clazz;
		this.item = is;
		this.runnables = runnables;
	}
	
	public static LinkedList<RequirementCreator> getCreators(){
		return (LinkedList<RequirementCreator>) creators.clone();
	}
	
}
