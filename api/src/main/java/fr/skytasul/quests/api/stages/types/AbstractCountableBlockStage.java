package fr.skytasul.quests.api.stages.types;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.sound.sampled.Line;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.api.utils.BQBlock;
import fr.skytasul.quests.api.utils.CountableObject;
import fr.skytasul.quests.api.utils.CountableObject.MutableCountableObject;
import fr.skytasul.quests.gui.blocks.BlocksGUI;

public abstract class AbstractCountableBlockStage extends AbstractCountableStage<BQBlock> {
	
	protected AbstractCountableBlockStage(@NotNull StageController controller,
			@NotNull List<@NotNull CountableObject<BQBlock>> objects) {
		super(controller, objects);
	}

	@Override
	protected boolean objectApplies(@NotNull BQBlock object, Object other) {
		if (other instanceof Block) return object.applies((Block) other);
		return super.objectApplies(object, other);
	}

	@Override
	protected @NotNull String getName(@NotNull BQBlock object) {
		return object.getName();
	}

	@Override
	protected @NotNull Object serialize(@NotNull BQBlock object) {
		return object.getAsString();
	}

	@Override
	protected @NotNull BQBlock deserialize(@NotNull Object object) {
		return BQBlock.fromString((String) object);
	}
	
	public abstract static class AbstractCreator<T extends AbstractCountableBlockStage> extends StageCreation<T> {
		
		protected List<MutableCountableObject<BQBlock>> blocks;
		
		protected AbstractCreator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(getBlocksSlot(), getBlocksItem(), (p, item) -> {
				new BlocksGUI(blocks, obj -> {
					setBlocks(obj);
					reopenGUI(player, true);
				}).open(player);
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
				reopenGUI(player, true);
			}).open(p);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setBlocks(stage.getMutableObjects());
		}
		
	}
	
}