package fr.skytasul.quests.api.options;

import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class QuestOptionBoolean extends QuestOption<Boolean> {

	public QuestOptionBoolean(Class<? extends QuestOption<?>>... requiredQuestOptions) {
		super(requiredQuestOptions);
	}

	@Override
	public Object save() {
		return getValue();
	}

	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.getBoolean(key));
	}

	@Override
	public Boolean cloneValue(Boolean value) {
		return value;
	}

	@Override
	public @Nullable String getValueString() {
		return Boolean.toString(getValue());
	}

	public abstract String getName();

	public String getDescription() {
		return null;
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.itemSwitch(getName(), getValue(), formatDescription(getDescription()));
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		setValue(!getValue());
		ItemUtils.setSwitch(event.getClicked(), getValue());
	}

}
