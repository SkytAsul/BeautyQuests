package fr.skytasul.quests.api.stages;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.structure.QuestBranch;

public class StageType<T extends AbstractStage> {
	
	public final String id;
	public final Class<T> clazz;
	public final String name;
	public final StageDeserializationSupplier<T> deserializationSupplier;
	public final ItemStack item;
	public final StageCreationSupplier<T> creationSupplier;
	public final String[] dependencies;
	
	public StageType(String id, Class<T> clazz, String name, StageDeserializationSupplier<T> deserializationSupplier, ItemStack item, StageCreationSupplier<T> creationSupplier, String... dependencies) {
		this.id = id;
		this.clazz = clazz;
		this.name = name;
		this.item = item;
		this.deserializationSupplier = deserializationSupplier;
		this.creationSupplier = creationSupplier;
		this.dependencies = dependencies;
	}
	
	public boolean isValid() {
		for (String depend : dependencies) {
			if (!Bukkit.getPluginManager().isPluginEnabled(depend)) return false;
		}
		return true;
	}
	
	@FunctionalInterface
	public static interface StageCreationSupplier<T extends AbstractStage> {
		
		StageCreation<T> supply(Line line, boolean endingStage);
		
	}
	
	@FunctionalInterface
	public static interface StageDeserializationSupplier<T extends AbstractStage> {
		
		T supply(Map<String, Object> serializedDatas, QuestBranch branch);
		
	}
	
}
