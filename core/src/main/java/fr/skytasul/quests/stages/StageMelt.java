package fr.skytasul.quests.stages;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.comparison.ItemComparisonMap;
import fr.skytasul.quests.api.stages.AbstractItemStage;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class StageMelt extends AbstractItemStage {
	
	public StageMelt(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> items, ItemComparisonMap comparisons) {
		super(branch, items, comparisons);
	}
	
	public StageMelt(QuestBranch branch, ConfigurationSection section) {
		super(branch, section);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMelt(FurnaceExtractEvent event) {
		event(PlayersManager.getPlayerAccount(event.getPlayer()), event.getPlayer(), new ItemStack(event.getItemType()), event.getItemAmount());
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_MELT.format(super.descriptionLine(acc, source));
	}
	
	public static StageMelt deserialize(ConfigurationSection section, QuestBranch branch) {
		return new StageMelt(branch, section);
	}

	public static class Creator extends AbstractItemStage.Creator<StageMelt> {
		
		private static final ItemStack editItems = ItemUtils.item(XMaterial.FURNACE, Lang.editItemsToMelt.toString());
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected ItemStack getEditItem() {
			return editItems;
		}
		
		@Override
		protected StageMelt finishStage(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> itemsMap, ItemComparisonMap comparisons) {
			return new StageMelt(branch, itemsMap, comparisons);
		}
		
	}

}
