package fr.skytasul.quests.api.requirements;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

public class RequirementCreator<T extends AbstractRequirement> {
	public static final LinkedList<RequirementCreator<?>> creators = new LinkedList<>();
	
	public final Class<T> clazz;
	public final ItemStack item;
	public final RequirementCreationRunnables<T> runnables;
	
	public RequirementCreator(Class<T> clazz, ItemStack is, RequirementCreationRunnables<T> runnables) {
		this.clazz = clazz;
		this.item = is;
		this.runnables = runnables;
	}
	
	public static LinkedList<RequirementCreator<?>> getCreators() {
		return creators;
	}
	
}
