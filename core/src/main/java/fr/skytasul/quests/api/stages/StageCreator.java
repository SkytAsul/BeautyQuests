package fr.skytasul.quests.api.stages;

import java.util.LinkedList;

import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.stages.StageCreation.StageCreationSupplier;

public class StageCreator<T extends AbstractStage> {
	public static final LinkedList<StageCreator<?>> creators = new LinkedList<>();
	
	public final StageType type;
	public final ItemStack item;
	public final StageCreationSupplier<T> stageCreationSupplier;
	
	public StageCreator(StageType type, ItemStack is, StageCreationSupplier<T> stageCreationSupplier) {
		this.type = type;
		this.item = is;
		this.stageCreationSupplier = stageCreationSupplier;
	}
	
	public static StageCreator<?> getCreator(StageType type) {
		for (StageCreator<?> creator : creators) {
			if (creator.type == type) return creator;
		}
		return null;
	}
	
}
