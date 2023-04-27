package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.utils.types.CountableObject;

public class StageEatDrink extends AbstractItemStage {
	
	public StageEatDrink(StageController controller, List<CountableObject<ItemStack>> objects, ItemComparisonMap comparisons) {
		super(branch, objects, comparisons);
	}
	
	public StageEatDrink(ConfigurationSection section, StageController controller) {
		super(branch, section);
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return Lang.SCOREBOARD_EAT_DRINK.format(super.descriptionLine(acc, source));
	}
	
	@EventHandler
	public void onItemConsume(PlayerItemConsumeEvent event) {
		event(PlayersManager.getPlayerAccount(event.getPlayer()), event.getPlayer(), event.getItem(), 1);
	}
	
	public static class Creator extends AbstractItemStage.Creator<StageEatDrink> {
		
		private static final ItemStack editItems = ItemUtils.item(XMaterial.COOKED_PORKCHOP, Lang.stageEatDrinkItems.toString());
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected ItemStack getEditItem() {
			return editItems;
		}
		
		@Override
		protected StageEatDrink finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageEatDrink(branch, items, comparisons);
		}
		
	}
	
}
