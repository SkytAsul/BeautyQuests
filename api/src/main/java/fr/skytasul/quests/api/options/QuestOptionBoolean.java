package fr.skytasul.quests.api.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;

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
	
	public abstract String getName();
	
	public String getDescription() {
		return null;
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.itemSwitch(getName(), getValue(), formatDescription(getDescription()));
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		setValue(!getValue());
		ItemUtils.set(item, getValue());
	}
	
}
