package fr.skytasul.quests.api.rewards;

import java.util.function.Supplier;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;

public class RewardCreator extends QuestObjectCreator<AbstractReward> {
	
	private boolean async = false;
	
	public RewardCreator(String id, Class<? extends AbstractReward> clazz, ItemStack is, Supplier<AbstractReward> newObjectSupplier) {
		super(id, clazz, is, newObjectSupplier);
	}
	
	public RewardCreator(String id, Class<? extends AbstractReward> clazz, ItemStack is, Supplier<AbstractReward> newObjectSupplier, boolean multiple, QuestObjectLocation... allowedLocations) {
		super(id, clazz, is, newObjectSupplier, multiple, allowedLocations);
	}
	
	public boolean canBeAsync() {
		return async;
	}
	
	public RewardCreator setCanBeAsync(boolean async) {
		this.async = async;
		return this;
	}
	
}
