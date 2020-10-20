package fr.skytasul.quests.options;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.api.options.QuestOptionObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionRequirements extends QuestOptionObject<AbstractRequirement> {
	
	@Override
	protected Function<AbstractRequirement, Map<String, Object>> getSerializeFunction() {
		return AbstractRequirement::serialize;
	}
	
	@Override
	protected AbstractRequirement deserialize(Map<String, Object> map) throws ClassNotFoundException {
		return AbstractRequirement.deserialize(map);
	}
	
	@Override
	protected String getSizeString(int size) {
		return Lang.requirements.format(size);
	}
	
	@Override
	protected String getInventoryName() {
		return Lang.INVENTORY_REQUIREMENTS.toString();
	}
	
	@Override
	protected Collection<QuestObjectCreator<AbstractRequirement>> getCreators() {
		return QuestsAPI.requirements.values();
	}
	
	@Override
	public XMaterial getItemMaterial() {
		return XMaterial.NETHER_STAR;
	}
	
	@Override
	public String getItemName() {
		return Lang.editRequirements.toString();
	}
	
	@Override
	public String getItemDescription() {
		return Lang.editRequirementsLore.toString();
	}
	
}
