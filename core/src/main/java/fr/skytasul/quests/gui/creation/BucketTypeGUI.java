package fr.skytasul.quests.gui.creation;

import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.templates.ChooseGUI;
import fr.skytasul.quests.stages.StageBucket.BucketType;
import fr.skytasul.quests.utils.Lang;

public class BucketTypeGUI extends ChooseGUI<BucketType>{

	private Consumer<BucketType> end;
	
	public BucketTypeGUI(Consumer<BucketType> end){
		super(Arrays.asList(BucketType.values()));
		this.end = end;
	}

	public String name(){
		return Lang.INVENTORY_BUCKETS.toString();
	}

	public ItemStack getItemStack(BucketType object){
		return ItemUtils.item(object.getMaterial(), object.getName());
	}

	public void finish(BucketType object){
		end.accept(object);
	}
	
}
