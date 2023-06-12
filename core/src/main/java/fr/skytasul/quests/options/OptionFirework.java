package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.utils.QuestUtils;

public class OptionFirework extends QuestOption<FireworkMeta> {
	
	@Override
	public Object save() {
		return getValue();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.getSerializable(key, FireworkMeta.class));
	}
	
	@Override
	public FireworkMeta cloneValue(FireworkMeta value) {
		return value.clone();
	}
	
	private List<String> getLore() {
		List<String> lore = new ArrayList<>(9);
		lore.add(QuestOption.formatDescription(Lang.optionFireworkLore.toString()));
		lore.add("");
		if (getValue() == null) {
			lore.add(Lang.NotSet.toString());
		}else {
			lore.add("§8> §7§l" + getValue().getEffectsSize() + " §8effect(s)");
			lore.add("§8> Power: §7§l" + getValue().getPower());
		}
		
		if (!hasCustomValue()) {
			lore.add("");
			lore.add(Lang.defaultValue.toString());
		}
		
		lore.add("");
		lore.add("§8" + Lang.ClickRight.toString() + " > §c" + Lang.Remove.toString());
		if (hasCustomValue()) lore.add("§8" + Lang.ClickShiftRight.toString() + " > §d" + Lang.Reset.toString());
		lore.add("§7§l" + Lang.optionFireworkDrop.toString());
		
		return lore;
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.FIREWORK_ROCKET, Lang.optionFirework.toString(), getLore());
	}
	
	@Override
	public void click(QuestCreationGuiClickEvent event) {
		if (event.hasCursor()) {
			ItemMeta cursorMeta = event.getCursor().getItemMeta();
			if (cursorMeta instanceof FireworkMeta) {
				setValue((FireworkMeta) cursorMeta);
				ItemUtils.lore(event.getClicked(), getLore());
				QuestUtils.runSync(() -> event.getPlayer().setItemOnCursor(null));
				Lang.FIREWORK_EDITED.send(event.getPlayer());
			} else {
				Lang.FIREWORK_INVALID.send(event.getPlayer());
			}
		}else {
			if (event.getClick() == ClickType.SHIFT_RIGHT) {
				resetValue();
				ItemUtils.lore(event.getClicked(), getLore());
			} else if (event.getClick() == ClickType.RIGHT) {
				setValue(null);
				Lang.FIREWORK_REMOVED.send(event.getPlayer());
				ItemUtils.lore(event.getClicked(), getLore());
			}
		}
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return QuestsConfiguration.getConfig().getQuestsConfig().fireworks();
	}
	
}
