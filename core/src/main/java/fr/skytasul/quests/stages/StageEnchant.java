package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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

public class StageEnchant extends AbstractItemStage {
	
	public StageEnchant(QuestBranch branch, List<CountableObject<ItemStack>> fishes, ItemComparisonMap comparisons) {
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
		protected StageEnchant finishStage(QuestBranch branch, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageEnchant(branch, items, comparisons);
		}
		
	}

}
