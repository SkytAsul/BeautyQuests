package fr.skytasul.quests.options;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.templates.PagedGUI;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.XMaterial;

public class OptionQuestPool extends QuestOption<QuestPool> {
	
	@Override
	public Object save() {
		return getValue().getID();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(BeautyQuests.getInstance().getPoolsManager().getPool(config.getInt(key)));
	}
	
	@Override
	public QuestPool cloneValue(QuestPool value) {
		return value;
	}
	
	private String[] getLore() {
		return new String[] { formatDescription("Attach this quest to a quest pool"), "", formatValue(getValue() == null ? null : "#" + getValue().getID()) };
	}

	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.CHEST, "§eQuest Pool", getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		new PagedGUI<QuestPool>("Quest pools", DyeColor.CYAN, BeautyQuests.getInstance().getPoolsManager().getPools()) {
			
			@Override
			public ItemStack getItemStack(QuestPool object) {
				return object.getItemStack();
			}
			
			@Override
			public void click(QuestPool existing) {
				setValue(existing);
				ItemUtils.lore(item, getLore());
				gui.reopen(p);
			}
			
			@Override
			public CloseBehavior onClose(Player p, Inventory inv) {
				Bukkit.getScheduler().runTask(BeautyQuests.getInstance(), () -> gui.reopen(p));
				return CloseBehavior.NOTHING;
			}
		}.create(p);
	}
	
}
