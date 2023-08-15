package fr.skytasul.quests.integrations;

import org.bukkit.inventory.ItemStack;
import dev.lone.itemsadder.api.CustomStack;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.localization.Lang;

public final class BQItemsAdder {

	private BQItemsAdder() {}

	private static final ItemComparison COMPARISON = new ItemComparison("items_adder", Lang.comparisonItemsAdder.toString(),
			Lang.comparisonItemsAdderLore.toString(), BQItemsAdder::compareItems).setHasPriority();

	public static void initialize() {
		QuestsAPI.getAPI().registerItemComparison(COMPARISON);
	}

	public static void unload() {
		QuestsAPI.getAPI().unregisterItemComparison(COMPARISON);
	}

	public static boolean compareItems(ItemStack item1, ItemStack item2) {
		CustomStack custom1 = CustomStack.byItemStack(item1);
		CustomStack custom2 = CustomStack.byItemStack(item2);

		if ((custom1 == null) != (custom2 == null))
			return false; // tests if 1 item is null and the other is not null => not the same items

		if (custom1 == null)
			return true; // means both items are null => same items

		return custom1.matchNamespacedID(custom2);
	}

}
