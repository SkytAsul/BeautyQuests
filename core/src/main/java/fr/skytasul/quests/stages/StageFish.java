package fr.skytasul.quests.stages;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
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
import fr.skytasul.quests.utils.types.CountableObject;

public class StageFish extends AbstractItemStage {
	
	public StageFish(QuestBranch branch, List<CountableObject<ItemStack>> fishes, ItemComparisonMap comparisons) {
		super(branch, fishes, comparisons);
	}
	
	public StageFish(QuestBranch branch, ConfigurationSection section) {
		super(branch, section);
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFish(PlayerFishEvent e){
		if (e.getState() == State.CAUGHT_FISH && e.getCaught() instanceof Item){
			Player p = e.getPlayer();
			PlayerAccount acc = PlayersManager.getPlayerAccount(p);
			Item item = (Item) e.getCaught();
			if (item.isDead() || !branch.hasStageLaunched(acc, this)) return;
			ItemStack fish = item.getItemStack();
			event(acc, p, fish, fish.getAmount());
		}
	}
	
	@Override
	protected String descriptionLine(PlayerAccount acc, Source source) {
		return Lang.SCOREBOARD_FISH.format(super.descriptionLine(acc, source));
	}
	
	public static StageFish deserialize(ConfigurationSection section, QuestBranch branch) {
		return new StageFish(branch, section);
	}

	public static class Creator extends AbstractItemStage.Creator<StageFish> {
		
		private static final ItemStack editFishesItem = ItemUtils.item(XMaterial.FISHING_ROD, Lang.editFishes.toString());
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}
		
		@Override
		protected ItemStack getEditItem() {
			return editFishesItem;
		}
		
		@Override
		protected StageFish finishStage(QuestBranch branch, List<CountableObject<ItemStack>> items, ItemComparisonMap comparisons) {
			return new StageFish(branch, items, comparisons);
		}
		
	}

}
