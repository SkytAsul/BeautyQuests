package fr.skytasul.quests.gui.items;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.QuestOption;
import org.bukkit.DyeColor;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ItemComparisonGUI extends PagedGUI<ItemComparison> {

	private ItemComparisonMap comparisons;

	public ItemComparisonGUI(ItemComparisonMap comparisons, Runnable validate) {
		super(Lang.INVENTORY_ITEM_COMPARISONS.toString(), DyeColor.LIME, QuestsAPI.getAPI().getItemComparisons(), x -> validate.run(),
				null);
		this.comparisons = comparisons;
	}

	@Override
	public ItemStack getItemStack(ItemComparison object) {
		return ItemUtils.itemSwitch(object.getItemName(), comparisons.isEnabled(object),
				QuestOption.formatDescription(object.getItemDescription()));
	}

	@Override
	public void click(ItemComparison existing, ItemStack item, ClickType clickType) {
		ItemUtils.setSwitch(item, comparisons.toggle(existing));
	}

}
