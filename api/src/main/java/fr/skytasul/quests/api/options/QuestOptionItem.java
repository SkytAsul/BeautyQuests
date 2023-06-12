package fr.skytasul.quests.api.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.misc.ItemGUI;

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
	
	private List<String> getLore() {
		List<String> lore = new ArrayList<>();
		
		if (getItemDescription() != null) lore.add(formatDescription(getItemDescription()));
		
		if (getValue() == null) {
			lore.add(Lang.NotSet.toString() + (hasCustomValue() ? "" : " " + Lang.defaultValue.toString()));
		}else {
			lore.add("");
			lore.add("§7Item name: §f" + ItemUtils.getName(getValue()));
			String[] itemLore = ItemUtils.getLore(getValue());
			if (itemLore != null) {
				lore.add("");
				lore.add("§7Item lore:");
				lore.addAll(Arrays.asList(itemLore));
			}
			if (!hasCustomValue()) {
				lore.add("");
				lore.add(Lang.defaultValue.toString());
			}
			lore.add("");
			lore.add("§8" + Lang.ClickShiftRight.toString() + " > §7" + Lang.Remove.toString());
		}
		
		return lore;
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		if (getValue() == null) return ItemUtils.item(getDefaultMaterial(), getItemName(), getLore());
		ItemStack item = getValue().clone();
		ItemUtils.name(item, getItemName());
		ItemUtils.lore(item, getLore());
		return item;
	}
	
	@Override
	public void click(QuestCreationGuiClickEvent event) {
		if (event.getClick() == ClickType.SHIFT_RIGHT) {
			setValue(null);
			event.getGui().updateOptionItem(this);
		}else {
			new ItemGUI(is -> {
				setValue(is);
				gui.inv.setItem(slot, getItemStack(null));
				gui.reopen(player);
			}, event::reopen).open(event.getPlayer());
		}
	}
	
	public abstract XMaterial getDefaultMaterial();
	
	public abstract String getItemName();
	
	public String getItemDescription() {
		return null;
	}
	
}
