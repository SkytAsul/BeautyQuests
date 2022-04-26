package fr.skytasul.quests.stages;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.AbstractCountableStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.creation.stages.StageRunnable;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.types.BQBlock;

public class StagePlaceBlocks extends AbstractCountableStage<BQBlock> {
	
	public StagePlaceBlocks(QuestBranch branch, Map<Integer, Entry<BQBlock, Integer>> blocks) {
		super(branch, blocks);
	}

	@Override
	public String descriptionLine(PlayerAccount acc, Source source){
		return Lang.SCOREBOARD_PLACE.format(super.descriptionLine(acc, source));
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onMine(BlockPlaceEvent e) {
		if (e.isCancelled() || e.getPlayer() == null) return;
		Player p = e.getPlayer();
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this)){
			event(acc, p, e.getBlock(), 1);
		}
	}
	
	@Override
	protected boolean objectApplies(BQBlock object, Object other) {
		if (other instanceof Block) return object.applies((Block) other);
		return super.objectApplies(object, other);
	}
	
	@Override
	protected String getName(BQBlock object) {
		return MinecraftNames.getMaterialName(object.getMaterial());
	}

	@Override
	protected Object serialize(BQBlock object) {
		return object.getAsString();
	}

	@Override
	protected BQBlock deserialize(Object object) {
		return BQBlock.fromString((String) object);
	}
	
	public static StagePlaceBlocks deserialize(ConfigurationSection section, QuestBranch branch) {
		StagePlaceBlocks stage = new StagePlaceBlocks(branch, new HashMap<>());
		stage.deserialize(section);
		return stage;
	}

	public static class Creator extends StageCreation<StagePlaceBlocks> {
		
		private Map<Integer, Entry<BQBlock, Integer>> blocks;
		
		public Creator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.STONE, Lang.editBlocksPlace.toString()), new StageRunnable() {
				@Override
				public void run(Player p, ItemStack item) {
					BlocksGUI blocksGUI = Inventories.create(p, new BlocksGUI());
					blocksGUI.setBlocksFromMap(blocks);
					blocksGUI.run = (obj) -> {
						setBlocks(obj);
						reopenGUI(p, true);
					};
				}
			});
		}
		
		public void setBlocks(Map<Integer, Entry<BQBlock, Integer>> blocks) {
			this.blocks = blocks;
			line.editItem(7, ItemUtils.lore(line.getItem(7), Lang.optionValue.format(blocks.size() + " blocks")));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			BlocksGUI blocks = Inventories.create(p, new BlocksGUI());
			blocks.run = (obj) -> {
				setBlocks(obj);
				reopenGUI(p, true);
			};
		}

		@Override
		public void edit(StagePlaceBlocks stage) {
			super.edit(stage);
			setBlocks(stage.cloneObjects());
		}

		@Override
		public StagePlaceBlocks finishStage(QuestBranch branch) {
			return new StagePlaceBlocks(branch, blocks);
		}
	}

}
