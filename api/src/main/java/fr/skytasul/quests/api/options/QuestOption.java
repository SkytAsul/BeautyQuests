package fr.skytasul.quests.api.options;

import java.util.Objects;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.AutoRegistered;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;

@AutoRegistered
public abstract class QuestOption<T> implements Cloneable {

	private final @NotNull QuestOptionCreator<T, QuestOption<T>> creator;

	private @Nullable T value;
	private @Nullable Quest attachedQuest;

	private final @NotNull Class<? extends QuestOption<?>> @NotNull [] requiredQuestOptions;
	private @Nullable Runnable valueUpdateListener;

	protected QuestOption(@NotNull Class<? extends QuestOption<?>> @NotNull... requiredQuestOptions) {
		this.requiredQuestOptions = requiredQuestOptions;

		this.creator = (QuestOptionCreator<T, QuestOption<T>>) QuestOptionCreator.creators.get(getClass());
		if (creator == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a quest option via the API.");

		setValue(creator.defaultValue == null ? null : cloneValue(creator.defaultValue));
	}

	public @NotNull QuestOptionCreator<T, QuestOption<T>> getOptionCreator() {
		return creator;
	}

	public final boolean hasCustomValue() {
		return !Objects.equals(this.value, creator.defaultValue);
	}

	public final @Nullable T getValue() {
		return value;
	}

	public void setValue(@Nullable T value) {
		this.value = value;
		valueUpdated();
	}

	public void resetValue() {
		setValue(creator.defaultValue);
	}

	protected void valueUpdated() {
		if (valueUpdateListener != null) valueUpdateListener.run();
	}

	public void setValueUpdaterListener(@Nullable Runnable listener) {
		this.valueUpdateListener = listener;
	}

	public @NotNull Class<? extends QuestOption<?>> @NotNull [] getRequiredQuestOptions() {
		return requiredQuestOptions;
	}

	public @Nullable Quest getAttachedQuest() {
		return attachedQuest;
	}

	public void attach(@NotNull Quest quest) {
		Validate.notNull(quest, "Attached quest cannot be null");
		if (this.attachedQuest != null)
			throw new IllegalStateException("This option is already attached to " + attachedQuest.getId());
		this.attachedQuest = quest;

		if (this instanceof Listener) {
			Bukkit.getPluginManager().registerEvents((Listener) this, QuestsPlugin.getPlugin());
		}

		if (this instanceof QuestDescriptionProvider) {
			quest.getDescriptions().add((QuestDescriptionProvider) this);
		}
	}

	public void detach() {
		Quest previous = this.attachedQuest;
		this.attachedQuest = null;

		if (this instanceof Listener) {
			HandlerList.unregisterAll((Listener) this);
		}

		if (previous != null && this instanceof QuestDescriptionProvider) {
			previous.getDescriptions().remove(this);
		}
	}

	public abstract @Nullable Object save();

	public abstract void load(@NotNull ConfigurationSection config, @NotNull String key);

	public abstract @Nullable T cloneValue(@Nullable T value);

	@Override
	public @NotNull QuestOption<T> clone() {
		QuestOption<T> clone = creator.optionSupplier.get();
		clone.setValue(value == null ? null : cloneValue(value));
		return clone;
	}

	public boolean shouldDisplay(@NotNull OptionSet options) {
		return true;
	}

	public void onDependenciesUpdated(@NotNull OptionSet options) {}

	public abstract @NotNull ItemStack getItemStack(@NotNull OptionSet options);

	public abstract void click(@NotNull QuestCreationGuiClickEvent event);

	public @NotNull String formatValue(@Nullable String valueString) {
		return formatNullableValue(valueString, !hasCustomValue());
	}

	public static @Nullable String formatDescription(@Nullable String description) {
		return description == null ? null : "§8> §7" + description;
	}

	public static @NotNull String formatNullableValue(@Nullable Object value) {
		return formatNullableValue(value, false);
	}

	public static @NotNull String formatNullableValue(@Nullable Object value, @NotNull Object defaultValue) {
		return formatNullableValue(value == null ? defaultValue : value, value == null);
	}

	public static @NotNull String formatNullableValue(@Nullable Object value, boolean isDefault) {
		String valueString =
				Lang.optionValue.format(PlaceholderRegistry.of("value", value == null ? Lang.NotSet.toString() : value));
		if (isDefault)
			valueString += " " + Lang.defaultValue.toString();
		return valueString;
	}

}
