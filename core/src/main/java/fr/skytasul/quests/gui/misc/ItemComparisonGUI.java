package fr.skytasul.quests.gui.misc;

import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.utils.Utils;

public class ItemComparisonGUI extends PagedGUI<ItemComparison> {
	
	private ItemComparisonMap comparisons;
	
	public ItemComparisonGUI(ItemComparisonMap comparisons, Runnable validate) {
		super("Item Comparisons", DyeColor.LIME, QuestsAPI.itemComparisons.values(), x -> validate.run(), null);
		this.comparisons = comparisons;
	}
	
	@Override
	public ItemStack getItemStack(ItemComparison object) {
		return ItemUtils.itemSwitch(object.getItemName(), comparisons.isEnabled(object), QuestOption.formatDescription(object.getItemDescription()));
	}

	@Override
	public void click(ItemComparison existing, ItemStack item, ClickType clickType) {
		ItemUtils.set(item, comparisons.toggle(existing));
	}
	
	public static void initialize() {
		QuestsAPI.registerItemComparison(new ItemComparison("bukkit", "Bukkit native comparison", "Uses Bukkit default item comparison system.\nCompares material, durability, nbt tags...", ItemStack::isSimilar).setEnabledByDefault());
		QuestsAPI.registerItemComparison(new ItemComparison("customBukkit", "Bukkit native comparison - NO NBT", "Uses Bukkit default item comparison system, but wipes out NBT tags.\nCompares material, durability...", Utils::isSimilar));
		QuestsAPI.registerItemComparison(new ItemComparison("material", "Item material", "Compares item material (i. e. stone, iron sword...)", (item1, item2) -> item2.getType() == item1.getType() && item2.getDurability() == item1.getDurability()));
		QuestsAPI.registerItemComparison(new ItemComparison("repair", "Repair cost", "Compares repair cost for armors and swords", (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			if (!(meta1 instanceof Repairable)) return true;
			ItemMeta meta2 = item2.getItemMeta();
			if (!(meta2 instanceof Repairable)) return true;
			return ((Repairable) meta1).getRepairCost() == ((Repairable) meta2).getRepairCost();
		}).setMetaNeeded());
		QuestsAPI.registerItemComparison(new ItemComparison("name", "Item name", "Compares items names", (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = item2.getItemMeta();
			return (meta1.hasDisplayName() == meta2.hasDisplayName()) && meta1.getDisplayName().equals(meta2.getDisplayName());
		}).setMetaNeeded());
		QuestsAPI.registerItemComparison(new ItemComparison("lore", "Item lore", "Compares items lores", (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = item2.getItemMeta();
			return (meta1.hasLore() == meta2.hasLore()) && meta1.getLore().equals(meta2.getLore());
		}).setMetaNeeded());
		QuestsAPI.registerItemComparison(new ItemComparison("enchants", "Item enchants", "Compares item enchants", (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = item2.getItemMeta();
			return (meta1.hasEnchants() == meta2.hasEnchants()) && meta1.getEnchants().equals(meta2.getEnchants());
		}).setMetaNeeded());
	}
	
}
