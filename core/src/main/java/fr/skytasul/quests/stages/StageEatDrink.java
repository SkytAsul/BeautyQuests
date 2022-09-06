package fr.skytasul.quests.stages;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class StageEatDrink extends AbstractItemStage {
	
	public StageEatDrink(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> objects, ItemComparisonMap comparisons) {
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
		protected StageEatDrink finishStage(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> itemsMap, ItemComparisonMap comparisons) {
			return new StageEatDrink(branch, itemsMap, comparisons);
		}
		
	}
	
}
