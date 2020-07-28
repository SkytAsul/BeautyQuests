package fr.skytasul.quests.api.rewards;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

public class RewardCreator<T extends AbstractReward> {
	public static final Map<Class<? extends AbstractReward>, RewardCreator<?>> creators = new HashMap<>();
	
	public final Class<T> clazz;
	public final ItemStack item;
	public final Supplier<T> newRewardSupplier;
	
	public RewardCreator(Class<T> clazz, ItemStack is, Supplier<T> newRewardSupplier) {
		this.clazz = clazz;
		this.item = is;
		this.newRewardSupplier = newRewardSupplier;
	}
	
}
