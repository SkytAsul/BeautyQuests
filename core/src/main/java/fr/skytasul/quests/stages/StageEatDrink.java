package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
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

public class StageEatDrink extends AbstractItemStage {
	
	public StageEatDrink(StageController controller, List<CountableObject<ItemStack>> objects, ItemComparisonMap comparisons) {
		super(controller, objects, comparisons);
	}
	
	public StageEatDrink(ConfigurationSection section, StageController controller) {
		super(controller, section);
	}
	
	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_EAT_DRINK.toString();
	}
	
	@EventHandler
	public void onItemConsume(PlayerItemConsumeEvent event) {
		event(event.getPlayer(), event.getItem(), 1);
	}
	
	public static class Creator extends AbstractItemStage.Creator<StageEatDrink> {
		
		private static final ItemStack editItems = ItemUtils.item(XMaterial.COOKED_PORKCHOP, Lang.stageEatDrinkItems.toString());
		
		public Creator(@NotNull StageCreationContext<StageEatDrink> context) {
			super(context);
		}

		@Override
		protected ItemStack getEditItem() {
			return editItems;
		}
		
		@Override
		protected StageEatDrink finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageEatDrink(controller, items, comparisons);
		}
		
	}
	
}
