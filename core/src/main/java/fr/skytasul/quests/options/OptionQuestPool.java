package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.gui.close.CloseBehavior;
import fr.skytasul.quests.api.gui.close.OpenCloseBehavior;
import fr.skytasul.quests.api.gui.templates.PagedGUI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import fr.skytasul.quests.structure.pools.QuestPoolControllerImplementation;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OptionQuestPool extends QuestOption<QuestPoolControllerImplementation> {

	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		if (getValue() != null) getValue().addQuest(quest);
	}

	@Override
	public Quest detach() {
		Quest previous = super.detach();
		if (getValue() != null)
			getValue().removeQuest(previous);
		return previous;
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
	public QuestPoolControllerImplementation cloneValue(QuestPoolControllerImplementation value) {
		return value;
	}

	@Override
	public @Nullable String getValueString() {
		if (getValue() == null)
			return null;
		String poolName = getValue().getPoolData().name();
		String poolId = "#" + getValue().getId();
		return poolName == null ? poolId : poolName + " " + poolId;
	}

	private List<String> getLore() {
		List<String> lore = new ArrayList<>(5);
		lore.add(formatDescription(Lang.questPoolLore.toString()));
		lore.add("");
		lore.add(formatValue());
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
			new PagedGUI<QuestPoolControllerImplementation>(Lang.INVENTORY_POOLS_LIST.toString(), DyeColor.CYAN,
					BeautyQuests.getInstance().getPoolsManager().getPools()) {

				@Override
				public ItemStack getItemStack(QuestPoolControllerImplementation object) {
					return ItemUtils.loreAdd(object.getItemStack(), "", Lang.poolChoose.toString());
				}

				@Override
				public void click(QuestPoolControllerImplementation existing, ItemStack poolItem, ClickType click) {
					setValue(existing);
					ItemUtils.lore(event.getClicked(), getLore());
					event.reopen();
				}

				@Override
				public CloseBehavior onClose(Player p) {
					return new OpenCloseBehavior(event.getGui());
				}
			}.addValidateButton(2, __ -> event.reopen()).open(event.getPlayer());
		}
	}

}
