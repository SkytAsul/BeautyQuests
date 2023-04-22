package fr.skytasul.quests.api.stages;

import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.serializable.SerializableCreator;
import fr.skytasul.quests.api.serializable.SerializableRegistry;
import fr.skytasul.quests.api.stages.options.StageOption;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.structure.QuestBranch;

public class StageType<T extends AbstractStage> {
	
	private final @NotNull String id;
	private final @NotNull Class<T> clazz;
	private final @NotNull String name;
	private final @NotNull StageLoader<T> loader;
	private final @NotNull ItemStack item;
	private final @NotNull StageCreationSupplier<T> creationSupplier;
	@Deprecated
	public final String[] dependencies; // TODO remove
	
	private final @NotNull SerializableRegistry<StageOption<T>, SerializableCreator<StageOption<T>>> optionsRegistry;
	
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
		this(id, clazz, name, loader, item, creationSupplier, new String[0]);
	}
	
	@Deprecated
	public StageType(String id, Class<T> clazz, String name, StageDeserializationSupplier<T> deserializationSupplier, ItemStack item, StageCreationSupplier<T> creationSupplier, String... dependencies) {
		this(id, clazz, name, (StageLoader<T>) deserializationSupplier, item, creationSupplier, dependencies);
	}
	
	@Deprecated
	public StageType(String id, Class<T> clazz, String name, StageLoader<T> loader, ItemStack item, StageCreationSupplier<T> creationSupplier, String... dependencies) {
		this.id = id;
		this.clazz = clazz;
		this.name = name;
		this.item = item;
		this.loader = loader;
		this.creationSupplier = creationSupplier;
		
		this.optionsRegistry = new SerializableRegistry<>("stage-options-" + id);
		
		this.dependencies = dependencies;
		if (dependencies.length != 0) BeautyQuests.logger.warning("Nag author of the " + id + " stage type about its use of the deprecated \"dependencies\" feature.");
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
	
	@Deprecated
	public boolean isValid() {
		for (String depend : dependencies) {
			if (!Bukkit.getPluginManager().isPluginEnabled(depend)) return false;
		}
		return true;
	}
	
	@FunctionalInterface
	public static interface StageCreationSupplier<T extends AbstractStage> {
		
		@NotNull
		StageCreation<T> supply(@NotNull Line line, boolean endingStage);
		
	}
	
	@FunctionalInterface
	@Deprecated
	public static interface StageDeserializationSupplier<T extends AbstractStage> extends StageLoader<T> {
		
		/**
		 * @deprecated for removal, {@link StageLoader#supply(ConfigurationSection, QuestBranch)} should be used instead.
		 */
		@Deprecated
		T supply(Map<String, Object> serializedDatas, QuestBranch branch);
		
		@Override
		default T supply(ConfigurationSection section, QuestBranch branch) {
			return supply(section.getValues(false), branch);
		}
		
	}
	
	@FunctionalInterface
	public static interface StageLoader<T extends AbstractStage> {
		
		@NotNull
		T supply(@NotNull ConfigurationSection section, @NotNull QuestBranch branch);
		
	}
	
}
