package fr.skytasul.quests.api.comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;

public class ItemComparisonMap implements Cloneable {
	
	private Map<String, Boolean> notDefault;
	private List<ItemComparison> effective;
	
	public ItemComparisonMap() {
		this(new HashMap<>());
	}
	
	public ItemComparisonMap(ConfigurationSection notDefault) {
		setNotDefaultComparisons(notDefault);
	}
	
	public ItemComparisonMap(Map<String, Boolean> notDefault) {
		setNotDefaultComparisons(notDefault);
	}
	
	public void setNotDefaultComparisons(ConfigurationSection section) {
		this.notDefault = (Map) section.getValues(false);
		
		effective = new ArrayList<>();
		for (ItemComparison comp : QuestsAPI.getItemComparisons()) {
			if (section.getBoolean(comp.getID(), comp.isEnabledByDefault())) effective.add(comp);
		}
	}
	
	public void setNotDefaultComparisons(Map<String, Boolean> comparisons) {
		this.notDefault = comparisons;
		
		effective = new ArrayList<>();
		for (ItemComparison comp : QuestsAPI.getItemComparisons()) {
			Boolean bool = notDefault.get(comp.getID());
			if (Boolean.FALSE.equals(bool)) continue;
			if (!comp.isEnabledByDefault() && !Boolean.TRUE.equals(bool)) continue;
			effective.add(comp);
		}
	}
	
	public Map<String, Boolean> getNotDefault() {
		return notDefault;
	}
	
	public boolean isDefault() {
		return notDefault.isEmpty();
	}
	
	public List<ItemComparison> getEffective() {
		return effective;
	}
	
	public boolean isEnabled(ItemComparison comparison) {
		return effective.contains(comparison);
	}
	
	public boolean toggle(ItemComparison comparison) {
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
			return true;
		}else {
			effective.remove(comparison);
			return false;
		}
	}
	
	public boolean isSimilar(ItemStack item1, ItemStack item2) {
		boolean meta1 = item1.hasItemMeta();
		boolean meta2 = item2.hasItemMeta();
		return effective.stream().allMatch(x -> {
			if (x.isMetaNeeded()) {
				if (meta1 != meta2) return false;
				if (!meta1) return true;
			}
			return x.isSimilar(item1, item2);
		});
	}
	
	public boolean containsItems(Inventory inv, ItemStack i, int amount) {
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
	
	public void removeItems(Inventory inv, ItemStack i) {
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
	public ItemComparisonMap clone() {
		return new ItemComparisonMap(new HashMap<>(notDefault));
	}
	
}
