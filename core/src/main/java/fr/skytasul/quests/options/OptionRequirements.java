package fr.skytasul.quests.options;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionObject;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.gui.quests.PlayerListGUI.Category;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public class OptionRequirements extends QuestOptionObject<AbstractRequirement, RequirementCreator> implements QuestDescriptionProvider {
	
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
	
	@Override
	public List<String> provideDescription(QuestDescriptionContext context) {
		if (!context.getPlayerAccount().isCurrent()) return null;
		if (!context.getDescriptionOptions().showRequirements()) return null;
		if (context.getCategory() != Category.NOT_STARTED) return null;
		
		List<String> requirements = getValue().stream()
				.map(x -> {
					String description = x.getDescription(context.getPlayerAccount().getPlayer());
					if (description != null) description = Utils.format(x.test(context.getPlayerAccount().getPlayer()) ? context.getDescriptionOptions().getRequirementsValid() : context.getDescriptionOptions().getRequirementsInvalid(), description);
					return description;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		if (requirements.isEmpty()) return null;
		
		requirements.add(0, Lang.RDTitle.toString());
		return requirements;
	}
	
	@Override
	public String getDescriptionId() {
		return "requirements";
	}

	@Override
	public double getDescriptionPriority() {
		return 30;
	}
	
}
