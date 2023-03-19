package fr.skytasul.quests.gui.creation;

import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ChooseGUI;
import fr.skytasul.quests.stages.StageBucket.BucketType;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class BucketTypeGUI extends ChooseGUI<BucketType>{

	private Runnable cancel;
	private Consumer<BucketType> end;
	
	public BucketTypeGUI(Runnable cancel, Consumer<BucketType> end) {
		super(Arrays.asList(BucketType.getAvailable()));
		this.cancel = cancel;
		this.end = end;
	}

	@Override
	public String name(){
		return Lang.INVENTORY_BUCKETS.toString();
	}

	@Override
	public ItemStack getItemStack(BucketType object){
		return ItemUtils.item(object.getMaterial(), object.getName());
	}

	@Override
	public void finish(BucketType object){
		end.accept(object);
	}
	
	@Override
	public CloseBehavior onClose(Player p, Inventory inv) {
		Utils.runSync(cancel);
		return CloseBehavior.NOTHING;
	}
	
}
