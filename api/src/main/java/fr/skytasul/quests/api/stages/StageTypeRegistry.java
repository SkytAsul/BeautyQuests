package fr.skytasul.quests.api.stages;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsPlugin;

public class StageTypeRegistry implements Iterable<StageType<?>> {
	
	private @NotNull List<@NotNull StageType<?>> types = new LinkedList<>();
	
	/**
	 * Registers new stage type into the plugin.
	 * @param type StageType instance
	 */
	public void register(@NotNull StageType<? extends AbstractStage> type) {
		Validate.notNull(type);
		types.add(type);
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Stage registered (" + type.getName() + ", " + (types.size() - 1) + ")");
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
