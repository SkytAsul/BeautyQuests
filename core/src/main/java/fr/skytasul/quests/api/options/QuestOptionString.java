package fr.skytasul.quests.api.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public abstract class QuestOptionString extends QuestOption<String> {
	
	public QuestOptionString(Class<? extends QuestOption<?>>... requiredQuestOptions) {
		super(requiredQuestOptions);
	}
	
	@Override
	public Object save() {
		return getValue();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.getString(key));
	}
	
	@Override
	public String cloneValue() {
		return getValue();
	}
	
	private String[] getLore() {
		if (getItemDescription() == null) {
			if (getValue() == null) return null;
			return new String[] { Lang.optionValue.format(getValue()) };
		}else {
			String description = formatDescription(getItemDescription());
			if (getValue() == null) return new String[] { description };
			return new String[] { description, "", Lang.optionValue.format(getValue()) };
		}
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(getItemMaterial(), getItemName(), getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		sendIndication(p);
		new TextEditor(p, (obj) -> {
			setValue((String) obj);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, () -> gui.reopen(p), () -> {
			resetValue();
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}).enterOrLeave(p);
	}
	
	public abstract void sendIndication(Player p);
	
	public abstract XMaterial getItemMaterial();
	
	public abstract String getItemName();
	
	public String getItemDescription() {
		return null;
	}
	
}
