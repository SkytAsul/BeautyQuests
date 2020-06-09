package fr.skytasul.quests.api.rewards;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

public class RewardCreator<T extends AbstractReward> {
	public static final LinkedList<RewardCreator<?>> creators = new LinkedList<>();
	
	public final Class<T> clazz;
	public final ItemStack item;
	public final RewardCreationRunnables<T> runnables;
	
	public RewardCreator(Class<T> clazz, ItemStack is, RewardCreationRunnables<T> runnables) {
		this.clazz = clazz;
		this.item = is;
		this.runnables = runnables;
	}
	
	public static LinkedList<RewardCreator<?>> getCreators() {
		return creators;
	}
	
}
