package fr.skytasul.quests.stages;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

public class StageEnchant extends AbstractItemStage {
	
	public StageEnchant(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> fishes, ItemComparisonMap comparisons) {
		super(branch, fishes, comparisons);
	}
	
	public StageEnchant(QuestBranch branch, ConfigurationSection section) {
		super(branch, section);
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnchant(EnchantItemEvent e) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(e.getEnchanter());
		if (!branch.hasStageLaunched(acc, this)) return;
		
		ItemStack finalItem = e.getItem().clone();
		ItemMeta meta = finalItem.getItemMeta();
		e.getEnchantsToAdd().forEach((enchant, level) -> meta.addEnchant(enchant, level, false));
		finalItem.setItemMeta(meta);
		
		event(acc, e.getEnchanter(), finalItem, e.getItem().getAmount());
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_ENCHANT.format(super.descriptionLine(acc, source));
	}
	
	public static StageEnchant deserialize(ConfigurationSection section, QuestBranch branch) {
		return new StageEnchant(branch, section);
	}

	public static class Creator extends AbstractItemStage.Creator<StageEnchant> {
		
		private static final ItemStack editItems = ItemUtils.item(XMaterial.ENCHANTING_TABLE, Lang.editItemsToEnchant.toString());
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected ItemStack getEditItem() {
			return editItems;
		}
		
		@Override
		protected StageEnchant finishStage(QuestBranch branch, Map<Integer, Entry<ItemStack, Integer>> itemsMap, ItemComparisonMap comparisons) {
			return new StageEnchant(branch, itemsMap, comparisons);
		}
		
	}

}
