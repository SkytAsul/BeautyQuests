package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.inventory.ItemStack;
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

public class StageMelt extends AbstractItemStage implements Listener {

	public StageMelt(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
		super(controller, items, comparisons);
	}

	public StageMelt(StageController controller, ConfigurationSection section) {
		super(controller, section);
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMelt(FurnaceExtractEvent event) {
		event(event.getPlayer(), new ItemStack(event.getItemType()), event.getItemAmount());
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_MELT.toString();
	}

	public static StageMelt deserialize(ConfigurationSection section, StageController controller) {
		return new StageMelt(controller, section);
	}

	public static class Creator extends AbstractItemStage.Creator<StageMelt> {

		private static final ItemStack editItems = ItemUtils.item(XMaterial.FURNACE, Lang.editItemsToMelt.toString());

		public Creator(@NotNull StageCreationContext<StageMelt> context) {
			super(context);
		}

		@Override
		protected ItemStack getEditItem() {
			return editItems;
		}

		@Override
		protected StageMelt finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageMelt(controller, items, comparisons);
		}

	}

}
