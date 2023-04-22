package fr.skytasul.quests.api.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsAPI;

public class ItemComparisonMap implements Cloneable {
	
	private Map<String, Boolean> notDefault;
	private List<ItemComparison> effective;
	
	public ItemComparisonMap() {
		this(new HashMap<>());
	}
	
	public ItemComparisonMap(@NotNull ConfigurationSection notDefault) {
		setNotDefaultComparisons(notDefault);
	}
	
	public ItemComparisonMap(@NotNull Map<String, Boolean> notDefault) {
		setNotDefaultComparisons(notDefault);
	}
	
	public void setNotDefaultComparisons(@NotNull ConfigurationSection section) {
		this.notDefault = (Map) section.getValues(false);
		
		effective = new ArrayList<>(3);
		for (ItemComparison comp : QuestsAPI.getItemComparisons()) {
			if (section.getBoolean(comp.getID(), comp.isEnabledByDefault()))
				effective.add(comp);
		}
		updated();
	}
	
	public void setNotDefaultComparisons(@NotNull Map<String, Boolean> comparisons) {
		this.notDefault = comparisons;

		effective = new ArrayList<>(3);
		for (ItemComparison comp : QuestsAPI.getItemComparisons()) {
			Boolean bool = notDefault.get(comp.getID());
			if (Boolean.FALSE.equals(bool)) continue;
			if (!comp.isEnabledByDefault() && !Boolean.TRUE.equals(bool)) continue;
			effective.add(comp);
		}
		updated();
	}
	
	public @NotNull Map<String, Boolean> getNotDefault() {
		return notDefault;
	}
	
	public boolean isDefault() {
		return notDefault.isEmpty();
	}
	
	public @NotNull List<@NotNull ItemComparison> getEffective() {
		return effective;
	}
	
	public boolean isEnabled(@NotNull ItemComparison comparison) {
		return effective.contains(comparison);
	}
	
	public boolean toggle(@NotNull ItemComparison comparison) {
		Boolean bool = notDefault.get(comparison.getID());
		if (bool == null) {
			bool = !comparison.isEnabledByDefault();
			notDefault.put(comparison.getID(), bool);
		}else {
			bool = !bool;
			if (bool.equals(comparison.isEnabledByDefault())) {
				notDefault.remove(comparison.getID());
			}else notDefault.put(comparison.getID(), bool);
		}
		if (bool) {
			effective.add(comparison);
			updated();
			return true;
		}else {
			effective.remove(comparison);
			return false;
		}
	}
	
	private void updated() {
		Collections.sort(effective, Comparator.comparing(ItemComparison::hasPriority));
	}

	public boolean isSimilar(@NotNull ItemStack item1, @NotNull ItemStack item2) {
		boolean meta1 = item1.hasItemMeta();
		boolean meta2 = item2.hasItemMeta();

		boolean lastResult = true;
		for (ItemComparison comparison : effective) {
			if (!comparison.hasPriority() && !lastResult) {
				// comparisons with priority are tested at the very beginning
				// if this comparison does not has priority and the last result is false then there is no need to go
				// further: we can stop there
				return false;
			}

			Boolean result = null;
			
			if (comparison.isMetaNeeded()) {
				if (meta1 != meta2)
					result = false;
				else if (!meta1)
					result = true;
			}
			
			if (result == null) result = comparison.isSimilar(item1, item2);
			
			if (result && comparison.hasPriority()) {
				// if the comparison has priority and matches those items then we can stop there
				return true;
			}

			lastResult = result;
		}
		
		return lastResult;
	}
	
	public boolean containsItems(@NotNull Inventory inv, @NotNull ItemStack i, int amount) {
		for (ItemStack item : inv.getContents()) {
			if (item == null) continue;
			if (isSimilar(item, i)) {
				if (item.getAmount() == amount) {
					return true;
				}
				if (item.getAmount() > amount) {
					return true;
				}else if (item.getAmount() < amount) {
					amount -= item.getAmount();
				}
			}
		}
		return false;
	}
	
	public void removeItems(@NotNull Inventory inv, @NotNull ItemStack i) {
		int amount = i.getAmount();
		if (amount <= 0) return;
		ItemStack[] items = inv.getContents();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			if (item == null) continue;
			if (isSimilar(item, i)) {
				if (item.getAmount() == amount) {
					inv.setItem(slot, new ItemStack(Material.AIR));
					return;
				}
				if (item.getAmount() > amount) {
					item.setAmount(item.getAmount() - amount);
					return;
				}else if (item.getAmount() < amount) {
					amount -= item.getAmount();
					inv.setItem(slot, new ItemStack(Material.AIR));
				}
			}
		}
	}
	
	@Override
	public @NotNull ItemComparisonMap clone() {
		return new ItemComparisonMap(new HashMap<>(notDefault));
	}
	
}
