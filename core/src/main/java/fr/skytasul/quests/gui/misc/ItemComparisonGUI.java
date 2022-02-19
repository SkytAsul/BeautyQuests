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
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class ItemComparisonGUI extends PagedGUI<ItemComparison> {
	
	private ItemComparisonMap comparisons;
	
	public ItemComparisonGUI(ItemComparisonMap comparisons, Runnable validate) {
		super(Lang.INVENTORY_ITEM_COMPARISONS.toString(), DyeColor.LIME, QuestsAPI.itemComparisons, x -> validate.run(), null);
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
		QuestsAPI.registerItemComparison(new ItemComparison("bukkit", Lang.comparisonBukkit.toString(), Lang.comparisonBukkitLore.toString(), ItemStack::isSimilar).setEnabledByDefault());
		QuestsAPI.registerItemComparison(new ItemComparison("customBukkit", Lang.comparisonCustomBukkit.toString(), Lang.comparisonCustomBukkitLore.toString(), Utils::isSimilar));
		QuestsAPI.registerItemComparison(new ItemComparison("material", Lang.comparisonMaterial.toString(), Lang.comparisonMaterialLore.toString(), (item1, item2) -> {
			if (item2.getType() != item1.getType()) return false;
			if (item1.getType().getMaxDurability() > 0 || XMaterial.isNewVersion()) return true;
			return item2.getDurability() == item1.getDurability();
		}));
		QuestsAPI.registerItemComparison(new ItemComparison("name", Lang.comparisonName.toString(), Lang.comparisonNameLore.toString(), (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = item2.getItemMeta();
			return (meta1.hasDisplayName() == meta2.hasDisplayName()) && meta1.getDisplayName().equals(meta2.getDisplayName());
		}).setMetaNeeded());
		QuestsAPI.registerItemComparison(new ItemComparison("lore", Lang.comparisonLore.toString(), Lang.comparisonLoreLore.toString(), (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = item2.getItemMeta();
			return (meta1.hasLore() == meta2.hasLore()) && meta1.getLore().equals(meta2.getLore());
		}).setMetaNeeded());
		QuestsAPI.registerItemComparison(new ItemComparison("enchants", Lang.comparisonEnchants.toString(), Lang.comparisonEnchantsLore.toString(), (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			ItemMeta meta2 = item2.getItemMeta();
			return (meta1.hasEnchants() == meta2.hasEnchants()) && meta1.getEnchants().equals(meta2.getEnchants());
		}).setMetaNeeded());
		QuestsAPI.registerItemComparison(new ItemComparison("repair", Lang.comparisonRepairCost.toString(), Lang.comparisonRepairCostLore.toString(), (item1, item2) -> {
			ItemMeta meta1 = item1.getItemMeta();
			if (!(meta1 instanceof Repairable)) return true;
			ItemMeta meta2 = item2.getItemMeta();
			if (!(meta2 instanceof Repairable)) return true;
			return ((Repairable) meta1).getRepairCost() == ((Repairable) meta2).getRepairCost();
		}).setMetaNeeded());
	}
	
}
