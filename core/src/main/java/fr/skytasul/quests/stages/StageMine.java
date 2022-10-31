package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.events.BQBlockBreakEvent;
import fr.skytasul.quests.api.stages.types.AbstractCountableBlockStage;
import fr.skytasul.quests.api.stages.types.Locatable;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;

@LocatableType (types = LocatedType.BLOCK)
public class StageMine extends AbstractCountableBlockStage implements Locatable.MultipleLocatable {

	private boolean placeCancelled;
	
	public StageMine(QuestBranch branch, Map<Integer, Entry<BQBlock, Integer>> blocks) {
		super(branch, blocks);
	}
	
	public boolean isPlaceCancelled() {
		return placeCancelled;
	}

	public void setPlaceCancelled(boolean cancelPlaced) {
		this.placeCancelled = cancelPlaced;
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_MINE.format(super.descriptionLine(acc, source));
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onMine(BQBlockBreakEvent e) {
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			for (Block block : e.getBlocks()) {
				if (placeCancelled && block.hasMetadata("playerInStage")) {
					if (block.getMetadata("playerInStage").get(0).asString().equals(p.getName())) return;
				}
				if (event(acc, p, block, 1)) return;
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlace(BlockPlaceEvent e){
		if (e.isCancelled() || !placeCancelled) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (!branch.hasStageLaunched(acc, this)) return;
		Map<Integer, Integer> playerBlocks = getPlayerRemainings(acc, true);
		if (playerBlocks == null) return;
		for (Integer id : playerBlocks.keySet()) {
			if (objectApplies(super.objects.get(id).getKey(), e.getBlock())) {
				e.getBlock().setMetadata("playerInStage", new FixedMetadataValue(BeautyQuests.getInstance(), p.getName()));
				return;
			}
		}
	}
	
	@Override
	public Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher) {
		return BQBlock.getNearbyBlocks(fetcher, objects.values().stream().map(Entry::getKey).collect(Collectors.toList()));
	}
	
	@Override
	protected void serialize(ConfigurationSection section) {
		super.serialize(section);
		if (placeCancelled) section.set("placeCancelled", placeCancelled);
	}
	
	public static StageMine deserialize(ConfigurationSection section, QuestBranch branch) {
		StageMine stage = new StageMine(branch, new HashMap<>());
		stage.deserialize(section);

		if (section.contains("placeCancelled")) stage.placeCancelled = section.getBoolean("placeCancelled");
		return stage;
	}

	public static class Creator extends AbstractCountableBlockStage.AbstractCreator<StageMine> {
		
		private boolean prevent = false;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(6, ItemUtils.itemSwitch(Lang.preventBlockPlace.toString(), prevent), (p, item) -> setPrevent(ItemUtils.toggle(item)));
		}
		
		@Override
		protected ItemStack getBlocksItem() {
			return ItemUtils.item(XMaterial.STONE_PICKAXE, Lang.editBlocksMine.toString());
		}
		
		public void setPrevent(boolean prevent) {
			if (this.prevent != prevent) {
				this.prevent = prevent;
				line.editItem(6, ItemUtils.set(line.getItem(6), prevent));
			}
		}

		@Override
		public void edit(StageMine stage) {
			super.edit(stage);
			setPrevent(stage.isPlaceCancelled());
		}
		
		@Override
		public StageMine finishStage(QuestBranch branch) {
			StageMine stage = new StageMine(branch, blocks);
			stage.setPlaceCancelled(prevent);
			return stage;
		}
	}

}
