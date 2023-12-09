package fr.skytasul.quests.api.rewards;

import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.objects.QuestObjectLocation;

public class RewardCreator extends QuestObjectCreator<AbstractReward> {
	
	private boolean async = false;
	
	public RewardCreator(@NotNull String id, @NotNull Class<? extends AbstractReward> clazz, @NotNull ItemStack is,
			@NotNull Supplier<AbstractReward> newObjectSupplier) {
		super(id, clazz, is, newObjectSupplier);
	}
	
	public RewardCreator(@NotNull String id, @NotNull Class<? extends AbstractReward> clazz, @NotNull ItemStack is,
			@NotNull Supplier<AbstractReward> newObjectSupplier, boolean multiple,
			@NotNull QuestObjectLocation @NotNull... allowedLocations) {
		super(id, clazz, is, newObjectSupplier, multiple, allowedLocations);
	}
	
	public boolean canBeAsync() {
		return async;
	}
	
	public @NotNull RewardCreator setCanBeAsync(boolean async) {
		this.async = async;
		return this;
	}
	
}
