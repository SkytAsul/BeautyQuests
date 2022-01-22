package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.skytasul.quests.api.options.UpdatableOptionSet.Updatable;

@SuppressWarnings ("rawtypes")
public abstract class UpdatableOptionSet<U extends Updatable> implements OptionSet {
	
	private Map<Class<? extends QuestOption<?>>, OptionWrapper> options = new HashMap<>();
	
	@Override
	public Iterator<QuestOption> iterator() {
		return options.values().stream().map(wrapper -> wrapper.option).iterator();
	}
	
	protected void addOption(QuestOption option, U updatable) {
		options.put((Class<? extends QuestOption<?>>) option.getClass(), new OptionWrapper(option, updatable));
	}
	
	@Override
	public <T extends QuestOption<?>> T getOption(Class<T> optionClass) {
		return (T) options.get(optionClass).option;
	}
	
	@Override
	public boolean hasOption(Class<? extends QuestOption<?>> clazz) {
		return options.containsKey(clazz);
	}
	
	protected OptionWrapper getWrapper(Class<? extends QuestOption<?>> optionClass) {
		return options.get(optionClass);
	}
	
	protected void calculateDependencies() {
		for (OptionWrapper wrapper : options.values()) {
			for (Class<? extends QuestOption<?>> requiredOptionClass : wrapper.option.getRequiredQuestOptions()) {
				options.get(requiredOptionClass).dependent.add(wrapper.updatable);
			}
		}
	}
	
	public class OptionWrapper {
		public final QuestOption option;
		public final U updatable;
		public final List<U> dependent = new ArrayList<>();
		
		public OptionWrapper(QuestOption option, U updatable) {
			this.option = option;
			this.updatable = updatable;
			option.setValueUpdaterListener(() -> dependent.forEach(U::update));
		}
	}
	
	public interface Updatable {
		void update();
	}
	
}
