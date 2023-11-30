package fr.skytasul.quests.api.options;

import java.util.*;

@SuppressWarnings ("rawtypes")
public class UpdatableOptionSet implements OptionSet {
	
	private Map<Class<? extends QuestOption<?>>, OptionWrapper> options = new HashMap<>();
	
	@Override
	public Iterator<QuestOption> iterator() {
		return options.values().stream().map(wrapper -> wrapper.option).iterator();
	}
	
	public void addOption(QuestOption option, Runnable update) {
		options.put((Class<? extends QuestOption<?>>) option.getClass(), new OptionWrapper(option, update));
	}
	
	@Override
	public <T extends QuestOption<?>> T getOption(Class<T> optionClass) {
		return (T) options.get(optionClass).option;
	}
	
	@Override
	public boolean hasOption(Class<? extends QuestOption<?>> clazz) {
		return options.containsKey(clazz);
	}
	
	public OptionWrapper getWrapper(Class<? extends QuestOption<?>> optionClass) {
		return options.get(optionClass);
	}
	
	public void calculateDependencies() {
		options.values().forEach(wrapper -> wrapper.dependent.clear());
		for (OptionWrapper wrapper : options.values()) {
			for (Class<? extends QuestOption<?>> requiredOptionClass : wrapper.option.getRequiredQuestOptions()) {
				options.get(requiredOptionClass).dependent.add(wrapper.update);
			}
		}
	}
	
	public class OptionWrapper {
		public final QuestOption option;
		public final Runnable update;
		public final List<Runnable> dependent = new ArrayList<>();
		
		public OptionWrapper(QuestOption option, Runnable update) {
			this.option = option;
			this.update = update;
			option.setValueUpdaterListener(() -> dependent.forEach(Runnable::run));
		}
	}
	
}
