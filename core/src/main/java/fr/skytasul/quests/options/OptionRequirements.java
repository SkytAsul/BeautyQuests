package fr.skytasul.quests.options;

import java.util.Map;
import java.util.function.Function;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionObject;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.XMaterial;

public class OptionRequirements extends QuestOptionObject<AbstractRequirement, RequirementCreator> {
	
	@Override
	protected Function<AbstractRequirement, Map<String, Object>> getSerializeFunction() {
		return AbstractRequirement::serialize;
	}
	
	@Override
	protected AbstractRequirement deserialize(Map<String, Object> map) {
		return AbstractRequirement.deserialize(map);
	}
	
	@Override
	protected String getSizeString(int size) {
		return Lang.requirements.format(size);
	}
	
	@Override
	protected QuestObjectsRegistry<AbstractRequirement, RequirementCreator> getObjectsRegistry() {
		return QuestsAPI.getRequirements();
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
