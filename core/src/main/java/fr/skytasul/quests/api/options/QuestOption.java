package fr.skytasul.quests.api.options;

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
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
		
		setValue(creator.defaultValue == null ? null : cloneValue(creator.defaultValue));
	}
	
	public QuestOptionCreator<T, QuestOption<T>> getOptionCreator() {
		return creator;
	}
	
	public final boolean hasCustomValue() {
		return !Objects.equals(this.value, creator.defaultValue);
	}
	
	public final T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
		valueUpdated();
	}
	
	public void resetValue() {
		setValue(creator.defaultValue);
	}
	
	protected void valueUpdated() {
		if (valueUpdateListener != null) valueUpdateListener.run();
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
		Validate.notNull(quest, "Attached quest cannot be null");
		if (this.attachedQuest != null) throw new IllegalStateException("This option is already attached to " + attachedQuest.getID());
		this.attachedQuest = quest;
		
		if (this instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) this, BeautyQuests.getInstance());
		}
	}
	
	public void detach() {
		this.attachedQuest = null;
		
		if (this instanceof Listener) {
			HandlerList.unregisterAll((Listener) this);
		}
	}
	
	public abstract Object save();
	
	public abstract void load(ConfigurationSection config, String key);
	
	public abstract T cloneValue(T value);
	
	@Override
	public QuestOption<T> clone() {
		QuestOption<T> clone = creator.optionSupplier.get();
		clone.setValue(value == null ? null : cloneValue(value));
		return clone;
	}
	
	public boolean shouldDisplay(OptionSet options) {
		return true;
	}
	
	public void updatedDependencies(OptionSet options, ItemStack item) {}
	
	public abstract ItemStack getItemStack(OptionSet options);
	
	public abstract void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click);
	
	public boolean clickCursor(FinishGUI gui, Player p, ItemStack item, ItemStack cursor, int slot) {
		return true;
	}
	
	public String formatValue(String valueString) {
		return formatNullableValue(valueString, !hasCustomValue());
	}
	
	public static String formatDescription(String description) {
		return description == null ? null : "§8> §7" + description;
	}
	
	public static String formatNullableValue(String valueString) {
		return formatNullableValue(valueString, false);
	}
	
	public static String formatNullableValue(String valueString, boolean defaultValue) {
		valueString = Lang.optionValue.format(valueString == null ? Lang.NotSet.toString() : valueString);
		if (defaultValue) valueString += " " + Lang.defaultValue.toString();
		return valueString;
	}
	
}
