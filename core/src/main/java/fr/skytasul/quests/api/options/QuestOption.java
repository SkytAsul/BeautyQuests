package fr.skytasul.quests.api.options;

import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;

public abstract class QuestOption<T> implements Cloneable {
	
	private final QuestOptionCreator<T, QuestOption<T>> creator;
	
	private T value;
	private Quest attachedQuest;
	
	private final Class<? extends QuestOption<?>>[] requiredQuestOptions;
	private Runnable valueUpdateListener;
	
	public QuestOption(Class<? extends QuestOption<?>>... requiredQuestOptions) {
		this.requiredQuestOptions = requiredQuestOptions;
		
		this.creator = (QuestOptionCreator<T, QuestOption<T>>) QuestOptionCreator.creators.get(getClass());
		if (creator == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a quest option via the API.");
		
		setValue(creator.defaultValue);
	}
	
	public QuestOptionCreator<T, QuestOption<T>> getOptionCreator() {
		return creator;
	}
	
	public boolean hasCustomValue() {
		return !Objects.equals(this.value, creator.defaultValue);
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
		if (valueUpdateListener != null) valueUpdateListener.run();
	}
	
	public void resetValue() {
		setValue(creator.defaultValue);
	}
	
	public void setValueUpdaterListener(Runnable listener) {
		this.valueUpdateListener = listener;
	}
	
	public Class<? extends QuestOption<?>>[] getRequiredQuestOptions() {
		return requiredQuestOptions;
	}
	
	public Quest getAttachedQuest() {
		return attachedQuest;
	}
	
	public void attach(Quest quest) {
		this.attachedQuest = quest;
	}
	
	public void detach() {
		this.attachedQuest = null;
	}
	
	public abstract Object save();
	
	public abstract void load(ConfigurationSection config, String key);
	
	public abstract T cloneValue();
	
	@Override
	public QuestOption<T> clone() {
		QuestOption<T> clone = creator.optionSupplier.get();
		clone.setValue(getValue() == null ? null : cloneValue());
		return clone;
	}
	
	public boolean shouldDisplay(OptionSet options) {
		return true;
	}
	
	public abstract ItemStack getItemStack();
	
	public abstract void click(FinishGUI gui, Player p, ItemStack item, int slot);
	
	public String formatValue(String valueString) {
		valueString = Lang.optionValue.format(valueString == null ? Lang.NotSet.toString() : valueString).toString();
		if (!hasCustomValue()) valueString += " " + Lang.defaultValue.toString();
		return valueString;
	}
	
	public static String formatDescription(String description) {
		return description == null ? null : "§8> §7" + description;
	}
	
}
