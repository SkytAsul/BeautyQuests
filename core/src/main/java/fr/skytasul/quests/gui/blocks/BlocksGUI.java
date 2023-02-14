package fr.skytasul.quests.gui.blocks;

import static fr.skytasul.quests.gui.ItemUtils.item;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.gui.templates.ListGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.types.BQBlock;
import fr.skytasul.quests.utils.types.CountableObject;
import fr.skytasul.quests.utils.types.CountableObject.MutableCountableObject;

public class BlocksGUI extends ListGUI<MutableCountableObject<BQBlock>> {

	private Consumer<List<MutableCountableObject<BQBlock>>> end;

	public BlocksGUI(Collection<MutableCountableObject<BQBlock>> blocks,
			Consumer<List<MutableCountableObject<BQBlock>>> end) {
		super(Lang.INVENTORY_BLOCKSLIST.toString(), DyeColor.GREEN, blocks);
		this.end = end;
	}
	
	@Override
	public void finish(List<MutableCountableObject<BQBlock>> objects) {
		end.accept(objects);
	}
	
	@Override
	public void createObject(Function<MutableCountableObject<BQBlock>, ItemStack> callback) {
		new SelectBlockGUI(true, (type, amount) -> {
			callback.apply(CountableObject.createMutable(UUID.randomUUID(), type, amount));
		}).create(p);
	}

	@Override
	public ItemStack getObjectItemStack(MutableCountableObject<BQBlock> object) {
		return item(object.getObject().getMaterial(), Lang.materialName.format(object.getObject().getAsString()),
				Lang.Amount.format(object.getAmount()));
	}

}
