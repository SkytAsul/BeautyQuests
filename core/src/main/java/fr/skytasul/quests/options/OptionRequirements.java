package fr.skytasul.quests.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class OptionRequirements extends QuestOption<List<AbstractRequirement>> {
	
	@Override
	public void attach(Quest quest) {
		super.attach(quest);
		getValue().forEach(requirement -> requirement.attach(quest));
	}
	
	@Override
	public void detach() {
		super.detach();
		getValue().forEach(AbstractRequirement::detach);
	}
	
	@Override
	public Object save() {
		return Utils.serializeList(getValue(), AbstractRequirement::serialize);
	}
	
	@Override
	public void load(ConfigurationSection config, String key) {
		List<Map<?, ?>> rlist = config.getMapList(key);
		for (Map<?, ?> rmap : rlist) {
			try {
				getValue().add(AbstractRequirement.deserialize((Map<String, Object>) rmap, getAttachedQuest()));
			}catch (Exception e) {
				BeautyQuests.getInstance().getLogger().severe("An exception occured while deserializing a requirement (class " + rmap.get("class") + ").");
				BeautyQuests.loadingFailure = true;
				e.printStackTrace();
				continue;
			}
		}
	}
	
	@Override
	public List<AbstractRequirement> cloneValue(List<AbstractRequirement> value) {
		return new ArrayList<>(value);
	}
	
	private String[] getLore() {
		return new String[] { formatDescription(Lang.editRequirementsLore.toString()), "", "§7" + Lang.requirements.format(getValue().size()) };
	}
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.item(XMaterial.NETHER_STAR, Lang.editRequirements.toString(), getLore());
	}
	
	@Override
	public void click(FinishGUI gui, Player p, ItemStack item, int slot) {
		new QuestObjectGUI<>(Lang.INVENTORY_REQUIREMENTS.toString(), QuestsAPI.requirements.values(), requirements -> {
			setValue(requirements);
			ItemUtils.lore(item, getLore());
			gui.reopen(p);
		}, getValue()).create(p);
	}
	
}
