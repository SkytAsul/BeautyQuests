package fr.skytasul.quests.api.comparison;

import java.util.function.BiPredicate;

import org.bukkit.inventory.ItemStack;

public class ItemComparison {
	
	private final String id, itemName, itemDescription;
	private final BiPredicate<ItemStack, ItemStack> comparator;
	private boolean enabledByDefault, needsMeta;
	
	public ItemComparison(String id, String itemName, String itemDescription, BiPredicate<ItemStack, ItemStack> comparator) {
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
	
	/**
	 * This must <i>not</i> check the amount of the item. The function call must not affect the item in any way.
	 */
	public boolean isSimilar(ItemStack item1, ItemStack item2) {
		return comparator.test(item1, item2);
	}
	
}
