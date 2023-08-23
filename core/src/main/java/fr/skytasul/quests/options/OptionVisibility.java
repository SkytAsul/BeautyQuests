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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.AbstractGui;
import fr.skytasul.quests.api.gui.GuiClickEvent;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.DelayCloseBehavior;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.XMaterial;

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
	public void click(QuestCreationGuiClickEvent event) {
		new VisibilityGUI(() -> {
			ItemUtils.lore(event.getClicked(), getLore());
			event.reopen();
		}).open(event.getPlayer());
	}
	
	class VisibilityGUI extends AbstractGui {
		
		private EnumMap<QuestVisibilityLocation, Boolean> locations = new EnumMap<>(QuestVisibilityLocation.class);
		private Runnable reopen;
		
		public VisibilityGUI(Runnable reopen) {
			this.reopen = reopen;
		}
		
		@Override
		protected Inventory instanciate(@NotNull Player player) {
			return Bukkit.createInventory(null, InventoryType.HOPPER, Lang.INVENTORY_VISIBILITY.toString());
		}

		@Override
		protected void populate(@NotNull Player player, @NotNull Inventory inventory) {
			for (int i = 0; i < 4; i++) {
				QuestVisibilityLocation loc = QuestVisibilityLocation.values()[i];
				boolean visible = getValue().contains(loc);
				locations.put(loc, visible);
				inventory.setItem(i, ItemUtils.itemSwitch(loc.getName(), visible));
			}
			inventory.setItem(4, ItemUtils.itemDone);
		}
		
		@Override
		public void onClick(GuiClickEvent event) {
			if (event.getSlot() >= 0 && event.getSlot() < 4) {
				locations.put(QuestVisibilityLocation.values()[event.getSlot()], ItemUtils.toggleSwitch(event.getClicked()));
			} else if (event.getSlot() == 4) {
				setValue(locations.entrySet().stream().filter(Entry::getValue).map(Entry::getKey).collect(Collectors.toList()));
				reopen.run();
			}
		}
		
		@Override
		public CloseBehavior onClose(Player p) {
			return new DelayCloseBehavior(reopen);
		}
		
	}
	
}
