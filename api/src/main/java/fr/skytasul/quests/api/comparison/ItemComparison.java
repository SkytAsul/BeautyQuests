package fr.skytasul.quests.api.comparison;

import java.util.function.BiPredicate;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemComparison {
	
	private final @NotNull String id, itemName, itemDescription;
	private final @NotNull BiPredicate<@NotNull ItemStack, @NotNull ItemStack> comparator;
	private boolean enabledByDefault, needsMeta, hasPriority;
	
	public ItemComparison(@NotNull String id, @NotNull String itemName, @NotNull String itemDescription,
			@NotNull BiPredicate<@NotNull ItemStack, @NotNull ItemStack> comparator) {
		this.id = id;
		this.itemName = itemName;
		this.itemDescription = itemDescription;
		this.comparator = comparator;
	}
	
	public String getID() {
		return id;
	}
	
	public String getItemName() {
		return itemName;
	}
	
	public String getItemDescription() {
		return itemDescription;
	}
	
	public ItemComparison setEnabledByDefault() {
		this.enabledByDefault = true;
		return this;
	}
	
	public boolean isEnabledByDefault() {
		return enabledByDefault;
	}
	
	public ItemComparison setMetaNeeded() {
		this.needsMeta = true;
		return this;
	}
	
	public boolean isMetaNeeded() {
		return needsMeta;
	}
	
	public ItemComparison setHasPriority() {
		this.hasPriority = true;
		return this;
	}

	public boolean hasPriority() {
		return hasPriority;
	}

	/**
	 * This must <i>not</i> check the amount of the item. The function call must not affect the item in any way.
	 */
	public boolean isSimilar(@NotNull ItemStack item1, @NotNull ItemStack item2) {
		return comparator.test(item1, item2);
	}
	
}
