package fr.skytasul.quests.options;

import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectsRegistry;
import fr.skytasul.quests.api.options.QuestOptionObject;
import fr.skytasul.quests.api.options.description.QuestDescriptionContext;
import fr.skytasul.quests.api.options.description.QuestDescriptionProvider;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.utils.PlayerListCategory;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class OptionRequirements extends QuestOptionObject<AbstractRequirement, RequirementCreator, RequirementList>
		implements QuestDescriptionProvider {

	@Override
	protected AbstractRequirement deserialize(Map<String, Object> map) {
		return AbstractRequirement.deserialize(map);
	}

	@Override
	protected String getSizeString() {
		return RequirementList.getSizeString(getValue().size());
	}

	@Override
	protected QuestObjectsRegistry<AbstractRequirement, RequirementCreator> getObjectsRegistry() {
		return QuestsAPI.getAPI().getRequirements();
	}

	@Override
	protected RequirementList instanciate(Collection<AbstractRequirement> objects) {
		return new RequirementList(objects);
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
		if (!context.getDescriptionOptions().showRequirements()) return null;
		if (context.getCategory() != PlayerListCategory.NOT_STARTED) return null;

		List<String> requirements = getValue().stream()
				.map(x -> {
					String description = x.getDescription(context.getPlayer());
					if (description != null)
						description =
								MessageUtils.format(x.isValid() && context.getPlayer() != null && x.test(context.getPlayer())
								? context.getDescriptionOptions().getRequirementsValid()
								: context.getDescriptionOptions().getRequirementsInvalid(),
								PlaceholderRegistry.of("requirement_description", description));
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
