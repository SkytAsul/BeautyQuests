package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.gui.creation.FinishGUI;

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
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		if (click == ClickType.SHIFT_RIGHT) {
			resetValue();
			ItemUtils.lore(item, getLore());
		}else if (click == ClickType.RIGHT) {
			setValue(null);
			Lang.FIREWORK_REMOVED.send(p);
			ItemUtils.lore(item, getLore());
		}
	}
	
	@Override
	public boolean clickCursor(FinishGUI gui, Player p, ItemStack item, ItemStack cursor, int slot) {
		ItemMeta cursorMeta = cursor.getItemMeta();
		if (cursorMeta instanceof FireworkMeta) {
			setValue((FireworkMeta) cursorMeta);
			ItemUtils.lore(item, getLore());
			Utils.runSync(() -> player.setItemOnCursor(null));
			Lang.FIREWORK_EDITED.send(p);
		}else {
			Lang.FIREWORK_INVALID.send(p);
		}
		return true;
	}
	
	@Override
	public boolean shouldDisplay(OptionSet options) {
		return QuestsConfiguration.doFireworks();
	}
	
}
