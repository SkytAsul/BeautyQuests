package fr.skytasul.quests.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.MaterialParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionQuestMaterial extends QuestOption<XMaterial> {
	
	@Override
	public void setValue(XMaterial value) {
		if (value == XMaterial.AIR) value = XMaterial.BOOK;
		super.setValue(value);
	}
	
	@Override
	public Object save() {
		return getValue().name();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(XMaterial.valueOf(config.getString(key)));
	}
	
	@Override
	public XMaterial cloneValue(XMaterial value) {
		return value;
	}
	
	private String[] getLore() {
		String description = formatDescription(Lang.customMaterialLore.toString());
		if (!hasCustomValue()) return new String[] { description, "", Lang.defaultValue.toString() };
		return new String[] { description };
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(getValue(), Lang.customMaterial.toString(), getLore());
	}

	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		Lang.QUEST_MATERIAL.send(p);
		new TextEditor(p, (obj) -> {
			setValue((XMaterial) obj);
			item.setType(getValue().parseMaterial());
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, new MaterialParser(false), () -> gui.reopen(p), () -> {
			resetValue();
			item.setType(getValue().parseMaterial());
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}).enterOrLeave(p);
	}
	
}
