package fr.skytasul.quests.api.stages.types;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.stream.Collectors;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.stages.types.Locatable.LocatableType;
import fr.skytasul.quests.api.stages.types.Locatable.LocatedType;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.BQBlock;

@LocatableType (types = LocatedType.BLOCK)
public abstract class AbstractCountableBlockStage extends AbstractCountableStage<BQBlock> implements Locatable.MultipleLocatable {
	
	protected AbstractCountableBlockStage(QuestBranch branch, Map<Integer, Entry<BQBlock, Integer>> objects) {
		super(branch, objects);
	}

	@Override
	protected boolean objectApplies(BQBlock object, Object other) {
		if (other instanceof Block) return object.applies((Block) other);
		return super.objectApplies(object, other);
	}

	@Override
	protected String getName(BQBlock object) {
		return object.getName();
	}

	@Override
	protected Object serialize(BQBlock object) {
		return object.getAsString();
	}

	@Override
	protected BQBlock deserialize(Object object) {
		return BQBlock.fromString((String) object);
	}
	
	@Override
	public Spliterator<Located> getNearbyLocated(NearbyFetcher fetcher) {
		return BQBlock.getNearbyBlocks(fetcher, objects.values().stream().map(Entry::getKey).collect(Collectors.toList()));
	}
	
	public abstract static class AbstractCreator<T extends AbstractCountableBlockStage> extends StageCreation<T> {
		
		protected Map<Integer, Entry<BQBlock, Integer>> blocks;
		
		protected AbstractCreator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(getBlocksSlot(), getBlocksItem(), (p, item) -> {
				BlocksGUI blocksGUI = Inventories.create(p, new BlocksGUI());
				blocksGUI.setBlocksFromMap(blocks);
				blocksGUI.run = obj -> {
					setBlocks(obj);
					reopenGUI(p, true);
				};
			});
		}
		
		protected abstract ItemStack getBlocksItem();
		
		protected int getBlocksSlot() {
			return 7;
		}
		
		public void setBlocks(Map<Integer, Entry<BQBlock, Integer>> blocks) {
			this.blocks = blocks;
			line.editItem(getBlocksSlot(), ItemUtils.lore(line.getItem(getBlocksSlot()), Lang.optionValue.format(blocks.size() + " blocks")));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			Inventories.create(p, new BlocksGUI()).run = obj -> {
				setBlocks(obj);
				reopenGUI(p, true);
			};
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setBlocks(stage.cloneObjects());
		}
		
	}
	
}