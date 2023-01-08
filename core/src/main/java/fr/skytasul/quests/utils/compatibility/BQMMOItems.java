package fr.skytasul.quests.utils.compatibility;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.utils.Lang;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.inventory.ItemStack;

public class BQMMOItems {

    private static final ItemComparison COMPARISON = new ItemComparison(
            "mmoItems",
            Lang.comparisonMmoItems.toString(),
            Lang.comparisonMmoItemsLore.toString(),
            BQMMOItems::compareMmoItems
    );

    private BQMMOItems() {}

    public static void initialize() {
        QuestsAPI.registerItemComparison(COMPARISON);
    }

    public static void disable() {
        QuestsAPI.unregisterItemComparison(COMPARISON);
    }

    private static boolean compareMmoItems(ItemStack item1, ItemStack item2) {
        Type type1 = MMOItems.getType(item1);
        Type type2 = MMOItems.getType(item2);
        if (type1 == null || !type1.equals(type2)) {
            return false;
        }

        String id1 = MMOItems.getID(item1);
        String id2 = MMOItems.getID(item2);
        return id1 != null && id1.equals(id2);
    }

}
