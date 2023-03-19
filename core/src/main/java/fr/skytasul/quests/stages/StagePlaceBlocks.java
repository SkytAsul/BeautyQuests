package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.stages.types.AbstractCountableBlockStage;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;
import fr.skytasul.quests.utils.types.CountableObject;

public class StagePlaceBlocks extends AbstractCountableBlockStage {
	
	public StagePlaceBlocks(QuestBranch branch, List<CountableObject<BQBlock>> blocks) {
		super(branch, blocks);
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_PLACE.format(super.descriptionLine(acc, source));
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e) {
		if (e.isCancelled()) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			event(acc, p, e.getBlock(), 1);
		}
	}
	
	public static StagePlaceBlocks deserialize(ConfigurationSection section, QuestBranch branch) {
		StagePlaceBlocks stage = new StagePlaceBlocks(branch, new ArrayList<>());
		stage.deserialize(section);
		return stage;
	}

	public static class Creator extends AbstractCountableBlockStage.AbstractCreator<StagePlaceBlocks> {
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
		}

		@Override
		protected ItemStack getBlocksItem() {
			return ItemUtils.item(XMaterial.STONE, Lang.editBlocksPlace.toString());
		}
		
		@Override
		public StagePlaceBlocks finishStage(QuestBranch branch) {
			return new StagePlaceBlocks(branch, getImmutableBlocks());
		}
	}

}
