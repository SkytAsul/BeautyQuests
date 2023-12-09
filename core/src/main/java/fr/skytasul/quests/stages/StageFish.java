package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
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

public class StageFish extends AbstractItemStage implements Listener {

	public StageFish(StageController controller, List<CountableObject<ItemStack>> fishes, ItemComparisonMap comparisons) {
		super(controller, fishes, comparisons);
	}

	public StageFish(StageController controller, ConfigurationSection section) {
		super(controller, section);
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFish(PlayerFishEvent e){
		if (e.getState() == State.CAUGHT_FISH && e.getCaught() instanceof Item){
			Player p = e.getPlayer();
			Item item = (Item) e.getCaught();
			if (item.isDead() || !hasStarted(p))
				return;
			ItemStack fish = item.getItemStack();
			event(p, fish, fish.getAmount());
		}
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_FISH.toString();
	}

	public static StageFish deserialize(ConfigurationSection section, StageController controller) {
		return new StageFish(controller, section);
	}

	public static class Creator extends AbstractItemStage.Creator<StageFish> {

		private static final ItemStack editFishesItem = ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString());

		public Creator(@NotNull StageCreationContext<StageFish> context) {
			super(context);
		}

		@Override
		protected ItemStack getEditItem() {
			return editFishesItem;
		}

		@Override
		protected StageFish finishStage(StageController controller, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageFish(controller, items, comparisons);
		}

	}

}
