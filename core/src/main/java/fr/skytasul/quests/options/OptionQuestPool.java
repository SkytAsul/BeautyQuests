package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.OpenCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.api.utils.XMaterial;

public class OptionQuestPool extends QuestOption<QuestPool> {
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		if (getValue() != null) getValue().addQuest(quest);
	}
	
	@Override
	public void detach() {
		if (getValue() != null) getValue().removeQuest(getAttachedQuest());
		super.detach();
	}
	
	@Override
	public Object save() {
		return getValue().getId();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(BeautyQuests.getInstance().getPoolsManager().getPool(config.getInt(key)));
	}
	
	@Override
	public QuestPool cloneValue(QuestPool value) {
		return value;
	}
	
	private List<String> getLore() {
		List<String> lore = new ArrayList<>(5);
		lore.add(formatDescription(Lang.questPoolLore.toString()));
		lore.add("");
		lore.add(formatValue(getValue() == null ? null : "#" + getValue().getId()));
		if (hasCustomValue()) {
			lore.add("");
			lore.add("§8" + Lang.ClickShiftRight.toString() + " > §d" + Lang.Reset.toString());
		}
		return lore;
	}

	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.CHEST, Lang.questPool.toString(), getLore());
	}
	
	@Override
	public void click(QuestCreationGuiClickEvent event) {
		if (event.getClick() == ClickType.SHIFT_RIGHT) {
			setValue(null);
			ItemUtils.lore(event.getClicked(), getLore());
		}else {
			new PagedGUI<QuestPool>(Lang.INVENTORY_POOLS_LIST.toString(), DyeColor.CYAN,
					BeautyQuests.getInstance().getPoolsManager().getPools(), list -> event.reopen(), null) {
				
				@Override
				public ItemStack getItemStack(QuestPool object) {
					return object.getItemStack(Lang.poolChoose.toString());
				}
				
				@Override
				public void click(QuestPool existing, ItemStack poolItem, ClickType click) {
					setValue(existing);
					ItemUtils.lore(event.getClicked(), getLore());
					event.reopen();
				}
				
				@Override
				public CloseBehavior onClose(Player p) {
					return new OpenCloseBehavior(event.getGui());
				}
			}.open(event.getPlayer());
		}
	}
	
}
