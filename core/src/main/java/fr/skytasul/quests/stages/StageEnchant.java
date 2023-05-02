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
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.types.AbstractItemStage;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.gui.creation.stages.Line;

public class StageEnchant extends AbstractItemStage {
	
	public StageEnchant(StageController controller, List<CountableObject<ItemStack>> fishes, ItemComparisonMap comparisons) {
		super(branch, fishes, comparisons);
	}
	
	public StageEnchant(StageController controller, ConfigurationSection section) {
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
	protected String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return Lang.SCOREBOARD_ENCHANT.format(super.descriptionLine(acc, source));
	}
	
	public static StageEnchant deserialize(ConfigurationSection section, StageController controller) {
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
		protected StageEnchant finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageEnchant(branch, items, comparisons);
		}
		
	}

}
