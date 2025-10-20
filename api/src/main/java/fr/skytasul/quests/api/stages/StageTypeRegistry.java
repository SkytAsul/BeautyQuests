package fr.skytasul.quests.api.stages;

import fr.skytasul.quests.api.stages.options.StageOptionAutoRegister;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class StageTypeRegistry implements Iterable<StageType<?>> {

	private static final LoggerExpanded LOGGER = LoggerExpanded.get("BeautyQuests.Stages");

	private final @NotNull List<@NotNull StageType<?>> types = new ArrayList<>();
	private final @NotNull List<@NotNull StageOptionAutoRegister> autoRegisteringOptions = new ArrayList<>(2);

	/**
	 * Registers new stage type into the plugin.
	 * @param type StageType instance
	 */
	public void register(@NotNull StageType<? extends AbstractStage> type) {
		Validate.notNull(type);
		types.add(type);
		LOGGER.debug("Registered ({0}, {1})", type.getName(), types.indexOf(type));

		for (StageOptionAutoRegister autoRegister : autoRegisteringOptions)
			applyAutoregisteringOptions(type, autoRegister);
	}

	private <T extends AbstractStage> void applyAutoregisteringOptions(@NotNull StageType<T> type,
			@NotNull StageOptionAutoRegister autoRegister) {
		if (autoRegister.appliesTo(type))
			type.getOptionsRegistry().register(autoRegister.createOptionCreator(type));
	}

	public void autoRegisterOption(@NotNull StageOptionAutoRegister autoRegister) {
		Validate.notNull(autoRegister);
		autoRegisteringOptions.add(autoRegister);

		for (StageType<?> type : types)
			applyAutoregisteringOptions(type, autoRegister);
	}

	public @NotNull List<@NotNull StageType<?>> getTypes() {
		return types;
	}

	public <T extends AbstractStage> @NotNull Optional<StageType<T>> getType(@NotNull Class<T> stageClass) {
		return types
				.stream()
				.filter(type -> type.getStageClass() == stageClass)
				.map(type -> (StageType<T>) type)
				.findAny();
	}

	public @NotNull Optional<StageType<?>> getType(@NotNull String id) {
		return types
				.stream()
				.filter(type -> type.getID().equals(id))
				.findAny();
	}

	@Override
	public Iterator<StageType<?>> iterator() {
		return types.iterator();
	}

}
