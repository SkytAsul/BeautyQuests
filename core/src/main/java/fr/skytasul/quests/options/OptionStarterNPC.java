package fr.skytasul.quests.options;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.npc.SelectGUI;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class OptionStarterNPC extends QuestOption<NPC> {
	
	@Override
	public Object save() {
		return getValue().getId();
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		setValue(CitizensAPI.getNPCRegistry().getById(config.getInt(key)));
	}
	
	@Override
	public NPC cloneValue(NPC value) {
		return value;
	}
	
	private String[] getLore() {
		return new String[] { formatDescription(Lang.questStarterSelectLore.toString()), "", getValue() == null ? Lang.Unknown.toString() : "§7" + getValue().getName() + " §8(" + getValue().getId() + ")" };
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.VILLAGER_SPAWN_EGG, Lang.questStarterSelect.toString(), getLore());
	}

	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		new SelectGUI(npc -> {
			setValue(npc);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}).create(p);
	}
	
}
