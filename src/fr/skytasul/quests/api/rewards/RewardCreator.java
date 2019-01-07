package fr.skytasul.quests.api.rewards;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

public class RewardCreator {
	public static final LinkedList<RewardCreator> creators = new LinkedList<>();
	
	public final Class<? extends AbstractReward> clazz;
	public final ItemStack item;
	public final RewardCreationRunnables runnables;
	
	public RewardCreator(Class<? extends AbstractReward> clazz, ItemStack is, RewardCreationRunnables runnables){
		this.clazz = clazz;
		this.item = is;
		this.runnables = runnables;
	}
	
	public static LinkedList<RewardCreator> getCreators(){
		return (LinkedList<RewardCreator>) creators.clone();
	}
	
}
