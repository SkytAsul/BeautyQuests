package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.gui.CustomInventory;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.gui.creation.FinishGUI;

public class OptionVisibility extends QuestOption<List<QuestVisibilityLocation>> {
	
	@Override
	public Object save() {
		return getValue().stream().map(QuestVisibilityLocation::name).collect(Collectors.toList());
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		if (config.isBoolean(key)) {
			setValue(Collections.emptyList()); // migration from before 0.20, where it was the "hide" option
		}else {
			setValue(config.getStringList(key).stream().map(QuestVisibilityLocation::valueOf).collect(Collectors.toList()));
		}
	}
	
	@Override
	public List<QuestVisibilityLocation> cloneValue(List<QuestVisibilityLocation> value) {
		return new ArrayList<>(value);
	}
	
	private String[] getLore() {
		return new String[] { formatDescription(Lang.optionVisibilityLore.toString()), "", formatValue(getValue().stream().map(QuestVisibilityLocation::getName).collect(Collectors.joining(", "))) };
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.SPYGLASS.or(XMaterial.BOOKSHELF), Lang.optionVisibility.toString(), getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		new VisibilityGUI(() -> {
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}).open(p);
	}
	
	class VisibilityGUI extends CustomInventory {
		
		private EnumMap<QuestVisibilityLocation, Boolean> locations = new EnumMap<>(QuestVisibilityLocation.class);
		private Runnable reopen;
		
		public VisibilityGUI(Runnable reopen) {
			this.reopen = reopen;
		}
		
		@Override
		public Inventory open(Player p) {
			Inventory inv = Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_VISIBILITY.toString());
			
			for (int i = 0; i < 4; i++) {
				QuestVisibilityLocation loc = QuestVisibilityLocation.values()[i];
				boolean visible = getValue().contains(loc);
				locations.put(loc, visible);
				inv.setItem(i, ItemUtils.itemSwitch(loc.getName(), visible));
			}
			inv.setItem(4, ItemUtils.itemDone);
			
			return p.openInventory(inv).getTopInventory();
		}
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			if (slot >= 0 && slot < 4) {
				locations.put(QuestVisibilityLocation.values()[slot], ItemUtils.toggle(current));
			}else if (slot == 4) {
				setValue(locations.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toList()));
				reopen.run();
			}
			return true;
		}
		
		@Override
		public CloseBehavior onClose(Player p) {
			return new DelayCloseBehavior(reopen);
		}
		
	}
	
}
