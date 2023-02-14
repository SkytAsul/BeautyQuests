package fr.skytasul.quests.api.stages.types;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.blocks.BlocksGUI;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.BQBlock;
import fr.skytasul.quests.utils.types.CountableObject;
import fr.skytasul.quests.utils.types.CountableObject.MutableCountableObject;

public abstract class AbstractCountableBlockStage extends AbstractCountableStage<BQBlock> {
	
	protected AbstractCountableBlockStage(QuestBranch branch, List<CountableObject<BQBlock>> objects) {
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
	
	public abstract static class AbstractCreator<T extends AbstractCountableBlockStage> extends StageCreation<T> {
		
		protected List<MutableCountableObject<BQBlock>> blocks;
		
		protected AbstractCreator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(getBlocksSlot(), getBlocksItem(), (p, item) -> {
				new BlocksGUI(blocks, obj -> {
					setBlocks(obj);
					reopenGUI(p, true);
				}).create(p);
			});
		}
		
		public List<CountableObject<BQBlock>> getImmutableBlocks() {
			return blocks.stream().map(MutableCountableObject::toImmutable).collect(Collectors.toList());
		}

		protected abstract ItemStack getBlocksItem();
		
		protected int getBlocksSlot() {
			return 7;
		}
		
		public void setBlocks(List<MutableCountableObject<BQBlock>> blocks) {
			this.blocks = blocks;
			line.editItem(getBlocksSlot(), ItemUtils.lore(line.getItem(getBlocksSlot()), Lang.optionValue.format(blocks.size() + " blocks")));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			new BlocksGUI(Collections.emptyList(), obj -> {
				setBlocks(obj);
				reopenGUI(p, true);
			}).create(p);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setBlocks(stage.getMutableObjects());
		}
		
	}
	
}