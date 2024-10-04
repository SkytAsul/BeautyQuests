package fr.skytasul.quests.stages;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.types.AbstractCountableBlockStage;
import fr.skytasul.quests.api.utils.CountableObject;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class StagePlaceBlocks extends AbstractCountableBlockStage implements Listener {

	public StagePlaceBlocks(StageController controller, List<CountableObject<BQBlock>> blocks) {
		super(controller, blocks);
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_PLACE.toString();
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e) {
		if (e.isCancelled())
			return;
		event(e.getPlayer(), e.getBlock(), 1);
	}

	public static StagePlaceBlocks deserialize(ConfigurationSection section, StageController controller) {
		StagePlaceBlocks stage = new StagePlaceBlocks(controller, new ArrayList<>());
		stage.deserialize(section);
		return stage;
	}

	public static class Creator extends AbstractCountableBlockStage.AbstractCreator<StagePlaceBlocks> {

		public Creator(@NotNull StageCreationContext<StagePlaceBlocks> context) {
			super(context);
		}

		@Override
		protected ItemStack getBlocksItem() {
			return ItemUtils.item(XMaterial.STONE, Lang.editBlocksPlace.toString());
		}

		@Override
		public StagePlaceBlocks finishStage(StageController controller) {
			return new StagePlaceBlocks(controller, getImmutableBlocks());
		}
	}

}
