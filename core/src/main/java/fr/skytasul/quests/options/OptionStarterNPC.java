package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.quests.creation.QuestCreationGuiClickEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class OptionStarterNPC extends QuestOption<BqNpc> {
	
	public OptionStarterNPC() {
		super(OptionQuestPool.class);
	}
	
	@Override
	public Object save() {
		return getValue().getId();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(QuestsPlugin.getPlugin().getNpcManager().getById(config.getString(key)));
	}
	
	@Override
	public BqNpc cloneValue(BqNpc value) {
		return value;
	}
	
	private List<String> getLore(OptionSet options) {
		List<String> lore = new ArrayList<>(4);
		lore.add(formatDescription(Lang.questStarterSelectLore.toString()));
		lore.add(null);
		if (options != null && options.hasOption(OptionQuestPool.class) && options.getOption(OptionQuestPool.class).hasCustomValue()) lore.add(Lang.questStarterSelectPool.toString());
		lore.add(getValue() == null ? Lang.NotSet.toString()
				: "§7" + getValue().getNpc().getName() + " §8(" + getValue().getId() + ")");
		return lore;
	}
	
	@Override
	public ItemStack getItemStack(OptionSet options) {
		return ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.questStarterSelect.toString(), getLore(options));
	}

	@Override
	public void click(QuestCreationGuiClickEvent event) {
		QuestsPlugin.getPlugin().getGuiManager().getFactory().createNpcSelection(event::reopen, npc -> {
			setValue(npc);
			ItemUtils.lore(event.getClicked(), getLore(event.getGui().getOptionSet()));
			event.reopen();
		}, true).open(event.getPlayer());
	}

}
