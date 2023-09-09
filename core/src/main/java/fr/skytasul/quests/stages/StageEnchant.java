package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.XMaterial;

public class StageEnchant extends AbstractItemStage implements Listener {

	public StageEnchant(StageController controller, List<CountableObject<ItemStack>> fishes, ItemComparisonMap comparisons) {
		super(controller, fishes, comparisons);
	}

	public StageEnchant(StageController controller, ConfigurationSection section) {
		super(controller, section);
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnchant(EnchantItemEvent e) {
		if (!hasStarted(e.getEnchanter()))
			return;

		ItemStack finalItem = e.getItem().clone();
		ItemMeta meta = finalItem.getItemMeta();
		e.getEnchantsToAdd().forEach((enchant, level) -> meta.addEnchant(enchant, level, false));
		finalItem.setItemMeta(meta);

		event(e.getEnchanter(), finalItem, e.getItem().getAmount());
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_ENCHANT.toString();
	}

	public static StageEnchant deserialize(ConfigurationSection section, StageController controller) {
		return new StageEnchant(controller, section);
	}

	public static class Creator extends AbstractItemStage.Creator<StageEnchant> {

		private static final ItemStack editItems = ItemUtils.item(XMaterial.ENCHANTING_TABLE, Lang.editItemsToEnchant.toString());

		public Creator(@NotNull StageCreationContext<StageEnchant> context) {
			super(context);
		}

		@Override
		protected ItemStack getEditItem() {
			return editItems;
		}

		@Override
		protected StageEnchant finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageEnchant(controller, items, comparisons);
		}

	}

}
