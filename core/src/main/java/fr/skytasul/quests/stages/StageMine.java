package fr.skytasul.quests.stages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import com.cryptomorin.xseries.XMaterial;
import com.gestankbratwurst.playerblocktracker.PlayerBlockTracker;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.blocks.BQBlock;
import fr.skytasul.quests.api.events.internal.BQBlockBreakEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageDescriptionPlaceholdersContext;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.creation.StageGuiLine;
import fr.skytasul.quests.api.stages.types.AbstractCountableBlockStage;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.api.utils.CountableObject;

@LocatableType (types = LocatedType.BLOCK)
public class StageMine extends AbstractCountableBlockStage implements Locatable.MultipleLocatable {

	private boolean placeCancelled;
	
	public StageMine(StageController controller, List<CountableObject<BQBlock>> blocks) {
		super(controller, blocks);
	}
	
	public boolean isPlaceCancelled() {
		return placeCancelled;
	}

	public void setPlaceCancelled(boolean cancelPlaced) {
		this.placeCancelled = cancelPlaced;
	}

	@Override
	public @NotNull String getDefaultDescription(@NotNull StageDescriptionPlaceholdersContext context) {
		return Lang.SCOREBOARD_MINE.toString();
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onMine(BQBlockBreakEvent e) {
		Player p = e.getPlayer();
		if (!hasStarted(p))
			return;

		for (Block block : e.getBlocks()) {
			if (placeCancelled) {
				if (QuestsConfigurationImplementation.getConfiguration().usePlayerBlockTracker()) {
					if (PlayerBlockTracker.isTracked(block))
						return;
				} else {
					if (block.hasMetadata("playerInStage")
							&& (block.getMetadata("playerInStage").get(0).asString().equals(p.getName()))) {
						return;
					}
				}
			}
			if (event(p, block, 1))
				return;
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e){
		if (QuestsConfigurationImplementation.getConfiguration().usePlayerBlockTracker())
			return;
		if (e.isCancelled() || !placeCancelled)
			return;

		Player p = e.getPlayer();
		if (!hasStarted(p))
			return;

		Map<UUID, Integer> playerBlocks = getPlayerRemainings(PlayersManager.getPlayerAccount(p), true);
		if (playerBlocks == null) return;
		for (UUID id : playerBlocks.keySet()) {
			Optional<CountableObject<BQBlock>> object = getObject(id);
			if (object.isPresent() && objectApplies(object.get().getObject(), e.getBlock())) {
				// we cannot use Optional#ifPresent(...) as we must stop the loop
				e.getBlock().setMetadata("playerInStage", new FixedMetadataValue(BeautyQuests.getInstance(), p.getName()));
				return;
			}
		}
	}
	
	@Override
	public Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher) {
		return QuestsAPI.getAPI().getBlocksManager().getNearbyBlocks(fetcher,
				objects.stream().map(CountableObject::getObject).collect(Collectors.toList()));
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		super.serialize(section);
		if (placeCancelled) section.set("placeCancelled", placeCancelled);
	}
	
	public static StageMine deserialize(ConfigurationSection section, StageController controller) {
		StageMine stage = new StageMine(controller, new ArrayList<>());
		stage.deserialize(section);

		if (section.contains("placeCancelled")) stage.placeCancelled = section.getBoolean("placeCancelled");
		return stage;
	}

	public static class Creator extends AbstractCountableBlockStage.AbstractCreator<StageMine> {
		
		private boolean prevent = false;
		
		public Creator(@NotNull StageCreationContext<StageMine> context) {
			super(context);
		}

		@Override
		public void setupLine(@NotNull StageGuiLine line) {
			super.setupLine(line);
			
			line.setItem(6, ItemUtils.itemSwitch(Lang.preventBlockPlace.toString(), prevent), event -> setPrevent(!prevent));
		}
		
		@Override
		protected ItemStack getBlocksItem() {
			return ItemUtils.item(XMaterial.STONE_PICKAXE, Lang.editBlocksMine.toString());
		}
		
		public void setPrevent(boolean prevent) {
			if (this.prevent != prevent) {
				this.prevent = prevent;
				getLine().refreshItem(6, item -> ItemUtils.setSwitch(item, prevent));
			}
		}

		@Override
		public void edit(StageMine stage) {
			super.edit(stage);
			setPrevent(stage.isPlaceCancelled());
		}
		
		@Override
		public StageMine finishStage(StageController controller) {
			StageMine stage = new StageMine(controller, getImmutableBlocks());
			stage.setPlaceCancelled(prevent);
			return stage;
		}
	}

}
