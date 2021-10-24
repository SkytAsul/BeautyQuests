package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionStarterNPC extends QuestOption<BQNPC> {
	
	public OptionStarterNPC() {
		super(OptionQuestPool.class);
	}
	
	@Override
	public Object save() {
		return getValue().getId();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(QuestsAPI.getNPCsManager().getById(config.getInt(key)));
	}
	
	@Override
	public BQNPC cloneValue(BQNPC value) {
		return value;
	}
	
	private String[] getLore(OptionSet options) {
		List<String> lore = new ArrayList<>(4);
		lore.add(formatDescription(Lang.questStarterSelectLore.toString()));
		lore.add(null);
		if (options != null && options.hasOption(OptionQuestPool.class) && options.getOption(OptionQuestPool.class).hasCustomValue()) lore.add(Lang.questStarterSelectPool.toString());
		lore.add(getValue() == null ? Lang.NotSet.toString() : "§7" + getValue().getName() + " §8(" + getValue().getId() + ")");
		return lore.toArray(new String[0]);
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.questStarterSelect.toString(), getLore(options));
	}

	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot, ClickType click) {
		new SelectGUI(() -> gui.reopen(p), npc -> {
			setValue(npc);
			ItemUtils.lore(item, getLore(gui));
			gui.reopen(p);
		}).setNullable().create(p);
	}
	
	@Override
	public void updatedDependencies(OptionSet options, ItemStack item) {
		super.updatedDependencies(options, item);
		ItemUtils.lore(item, getLore(options));
	}
	
}
