package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceExtractEvent;
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
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.gui.creation.stages.Line;

public class StageMelt extends AbstractItemStage {
	
	public StageMelt(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
		super(branch, items, comparisons);
	}
	
	public StageMelt(StageController controller, ConfigurationSection section) {
		super(branch, section);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMelt(FurnaceExtractEvent event) {
		event(PlayersManager.getPlayerAccount(event.getPlayer()), event.getPlayer(), new ItemStack(event.getItemType()), event.getItemAmount());
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, DescriptionSource source) {
		return Lang.SCOREBOARD_MELT.format(super.descriptionLine(acc, source));
	}
	
	public static StageMelt deserialize(ConfigurationSection section, StageController controller) {
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
		protected StageMelt finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageMelt(branch, items, comparisons);
		}
		
	}

}
