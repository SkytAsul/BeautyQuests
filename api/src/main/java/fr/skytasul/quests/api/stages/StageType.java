package fr.skytasul.quests.api.stages;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.serializable.SerializableRegistry;
import fr.skytasul.quests.api.stages.creation.StageCreation;
import fr.skytasul.quests.api.stages.creation.StageCreationContext;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.api.utils.messaging.HasPlaceholders;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

public class StageType<T extends AbstractStage> implements HasPlaceholders {
	
	private final @NotNull String id;
	private final @NotNull Class<T> clazz;
	private final @NotNull String name;
	private final @NotNull StageLoader<T> loader;
	private final @NotNull ItemStack item;
	private final @NotNull StageCreationSupplier<T> creationSupplier;
	
	private final @NotNull SerializableRegistry<StageOption<T>, SerializableCreator<StageOption<T>>> optionsRegistry;
	private @NotNull PlaceholderRegistry placeholders;
	
	/**
	 * Creates a stage type.
	 * 
	 * @param id unique string id for this stage
	 * @param clazz class of this stage
	 * @param name proper name of this stage
	 * @param loader function which instanciates and loads values of a previously saved stage
	 * @param item item representing this stage in the Stages GUI
	 * @param creationSupplier function creating a stage creation context
	 */
	public StageType(@NotNull String id, @NotNull Class<T> clazz, @NotNull String name, @NotNull StageLoader<T> loader,
			@NotNull ItemStack item, @NotNull StageCreationSupplier<T> creationSupplier) {
		this.id = id;
		this.clazz = clazz;
		this.name = name;
		this.item = item;
		this.loader = loader;
		this.creationSupplier = creationSupplier;
		
		this.optionsRegistry = new SerializableRegistry<>("stage-options-" + id);
	}
	
	public @NotNull String getID() {
		return id;
	}
	
	public @NotNull Class<T> getStageClass() {
		return clazz;
	}
	
	public @NotNull String getName() {
		return name;
	}
	
	public @NotNull StageLoader<T> getLoader() {
		return loader;
	}
	
	public @NotNull ItemStack getItem() {
		return item;
	}
	
	public @NotNull StageCreationSupplier<T> getCreationSupplier() {
		return creationSupplier;
	}
	
	public @NotNull SerializableRegistry<StageOption<T>, SerializableCreator<StageOption<T>>> getOptionsRegistry() {
		return optionsRegistry;
	}
	
	@Override
	public @NotNull PlaceholderRegistry getPlaceholdersRegistry() {
		return PlaceholderRegistry.of("stage_type", name, "stage_type_id", id);
	}

	@FunctionalInterface
	public static interface StageCreationSupplier<T extends AbstractStage> {
		
		@NotNull
		StageCreation<T> supply(@NotNull StageCreationContext<T> context);
		
	}
	
	@FunctionalInterface
	public static interface StageLoader<T extends AbstractStage> {
		
		@NotNull
		T supply(@NotNull ConfigurationSection section, @NotNull StageController controller);
		
	}
	
}
