package fr.skytasul.quests.api.stages;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;

import fr.skytasul.quests.utils.DebugUtils;

public class StageTypeRegistry implements Iterable<StageType<?>> {
	
	private List<StageType<?>> types = new LinkedList<>();
	
	/**
	 * Register new stage type into the plugin
	 * @param type StageType instance
	 */
	public void register(StageType<? extends AbstractStage> type) {
		Validate.notNull(type);
		types.add(type);
		DebugUtils.logMessage("Stage registered (" + type.name + ", " + (types.size() - 1) + ")");
	}
	
	public List<StageType<?>> getTypes() {
		return types;
	}
	
	public <T extends AbstractStage> Optional<StageType<T>> getType(Class<T> stageClass) {
		return types
				.stream()
				.filter(type -> type.clazz == stageClass)
				.map(type -> (StageType<T>) type)
				.findAny();
	}
	
	public Optional<StageType<?>> getType(String id) {
		return types
				.stream()
				.filter(type -> type.id.equals(id))
				.findAny();
	}
	
	@Override
	public Iterator<StageType<?>> iterator() {
		return types.iterator();
	}
	
}
