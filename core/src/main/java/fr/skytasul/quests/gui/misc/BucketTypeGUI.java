package fr.skytasul.quests.gui.misc;

import java.util.Arrays;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.gui.templates.ChooseGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.stages.StageBucket.BucketType;

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
	public CloseBehavior onClose(Player p) {
		return new DelayCloseBehavior(cancel);
	}
	
}
