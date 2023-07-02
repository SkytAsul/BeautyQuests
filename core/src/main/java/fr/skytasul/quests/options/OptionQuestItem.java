package fr.skytasul.quests.options;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.TextEditor;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.utils.QuestUtils;

public class OptionQuestItem extends QuestOption<ItemStack> {
	
	@Override
	public void setValue(ItemStack value) {
		if (value == null || value.getType() == Material.AIR) {
			value = XMaterial.BOOK.parseItem();
		}else {
			value = ItemUtils.clearVisibleAttributes(value.clone());
		}
		super.setValue(value);
	}
	
	@Override
	public Object save() {
		return getValue();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(config.isItemStack(key) ? config.getItemStack(key) : XMaterial.valueOf(config.getString(key)).parseItem());
	}
	
	@Override
	public ItemStack cloneValue(ItemStack value) {
		return value.clone();
	}
	
	private String[] getLore() {
		String description = formatDescription(Lang.customMaterialLore.toString());
		if (!hasCustomValue()) return new String[] { description, "", Lang.defaultValue.toString() };
		return new String[] { description };
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.nameAndLore(getValue().clone(), Lang.customMaterial.toString(), getLore());
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		if (event.hasCursor()) {
			QuestUtils.runSync(() -> event.getPlayer().setItemOnCursor(null));
			setValue(event.getCursor());
			ItemUtils.nameAndLore(event.getCursor(), Lang.customMaterial.toString(), getLore());
			event.setCancelled(false);
		} else {
			Lang.QUEST_MATERIAL.send(event.getPlayer());
			new TextEditor<>(event.getPlayer(), event::reopen, obj -> {
				if (obj == null) {
					resetValue();
				} else {
					setValue(obj.parseItem());
				}
				event.getGui().updateOptionItem(this);
				ItemStack setItem = event.getGui().getInventory().getItem(event.getSlot());
				if (setItem == null || setItem.getType() == Material.AIR) {
					// means that the material cannot be treated as an inventory item (ex: fire)
					resetValue();
					Lang.INVALID_ITEM_TYPE.send(event.getPlayer());
					event.getGui().updateOptionItem(this);
				}
				event.reopen();
			}, QuestsPlugin.getPlugin().getEditorManager().getFactory().getMaterialParser(true, true))
					.passNullIntoEndConsumer().start();
		}
	}
	
}
