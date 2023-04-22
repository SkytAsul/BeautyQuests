package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.CountableObject;

public class StageEatDrink extends AbstractItemStage {
	
	public StageEatDrink(QuestBranch branch, List<CountableObject<ItemStack>> objects, ItemComparisonMap comparisons) {
		super(branch, objects, comparisons);
	}
	
	public StageEatDrink(ConfigurationSection section, QuestBranch branch) {
		super(branch, section);
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
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
		protected StageEatDrink finishStage(QuestBranch branch, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageEatDrink(branch, items, comparisons);
		}
		
	}
	
}
