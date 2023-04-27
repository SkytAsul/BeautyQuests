package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.structure.QuestImplementation;
import fr.skytasul.quests.structure.pools.QuestPoolImplementation;

public class OptionQuestPool extends QuestOption<QuestPoolImplementation> {
	
	@Override
	public void attach(QuestImplementation quest) {
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
	public QuestPoolImplementation cloneValue(QuestPoolImplementation value) {
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
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		if (click == ClickType.SHIFT_RIGHT) {
			setValue(null);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}else {
			new PagedGUI<QuestPoolImplementation>(Lang.INVENTORY_POOLS_LIST.toString(), DyeColor.CYAN, BeautyQuests.getInstance().getPoolsManager().getPools(), list -> gui.reopen(p), null) {
				
				@Override
				public ItemStack getItemStack(QuestPoolImplementation object) {
					return object.getItemStack(Lang.poolChoose.toString());
				}
				
				@Override
				public void click(QuestPoolImplementation existing, ItemStack poolItem, ClickType click) {
					setValue(existing);
					ItemUtils.lore(item, getLore());
					gui.reopen(player);
				}
				
				@Override
				public CloseBehavior onClose(Player p, Inventory inv) {
					Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), () -> gui.reopen(p));
					return StandardCloseBehavior.NOTHING;
				}
			}.open(p);
		}
	}
	
}
