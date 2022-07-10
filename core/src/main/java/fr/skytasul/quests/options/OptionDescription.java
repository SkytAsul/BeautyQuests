package fr.skytasul.quests.options;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.options.QuestOptionString;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionDescription extends QuestOptionString implements QuestDescriptionProvider {
	
	private List<String> cachedDescription;
	
	@Override
	public void setValue(String value) {
		super.setValue(value);
		cachedDescription = null;
	}
	
	@Override
	public void sendIndication(Player p) {
		Lang.QUEST_DESCRIPTION.send(p);
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.OAK_SIGN;
	}
	
	@Override
	public String getItemName() {
		return Lang.customDescription.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.customDescriptionLore.toString();
	}
	
	@Override
	public boolean isMultiline() {
		return true;
	}
	
	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		if (cachedDescription == null) cachedDescription = Arrays.asList("§7" + getValue());
		return cachedDescription;
	}
	
	@Override
	public double getDescriptionPriority() {
		return 0;
	}
	
}
