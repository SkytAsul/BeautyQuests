package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.misc.ItemGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public abstract class QuestOptionItem extends QuestOption<ItemStack> {
	
	public QuestOptionItem(Class<? extends QuestOption<?>>... requiredQuestOptions) {
		super(requiredQuestOptions);
	}
	
	@Override
	public Object save() {
		return getValue().serialize();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(ItemStack.deserialize(Utils.mapFromConfigurationSection(config.getConfigurationSection(key))));
	}
	
	@Override
	public ItemStack cloneValue(ItemStack value) {
		return value.clone();
	}
	
	private String[] getLore() {
		List<String> lore = new ArrayList<>();
		
		if (getItemDescription() != null) lore.add(formatDescription(getItemDescription()));
		
		if (getValue() == null) {
			lore.add(Lang.NotSet.toString());
		}else {
			lore.add("");
			lore.add("Item name: " + ItemUtils.getName(getValue()));
			String[] itemLore = ItemUtils.getLore(getValue());
			if (itemLore != null) {
				lore.add("");
				lore.add("Item lore:");
				lore.addAll(Arrays.asList(itemLore));
			}
			if (!hasCustomValue()) {
				lore.add("");
				lore.add(Lang.defaultValue.toString());
			}
		}
		
		return lore.stream().toArray(String[]::new);
	}
	
	@Override
	public ItemStack getItemStack() {
		if (getValue() == null) return ItemUtils.item(getDefaultMaterial(), getItemName(), getLore());
		ItemStack item = getValue().clone();
		ItemUtils.name(item, getItemName());
		ItemUtils.lore(item, getLore());
		return item;
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		new ItemGUI(is -> {
			setValue(is);
			gui.inv.setItem(slot, getItemStack());
			gui.reopen(p);
		}).create(p);
	}
	
	public abstract XMaterial getDefaultMaterial();
	
	public abstract String getItemName();
	
	public String getItemDescription() {
		return null;
	}
	
}
