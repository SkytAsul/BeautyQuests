package fr.skytasul.quests.api.options.description;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.structure.Quest;

public class QuestDescription {
	
	private boolean requirements;
	private String requirementsValid;
	private String requirementsInvalid;
	
	private boolean rewards;
	private String rewardsFormat;
	
	public QuestDescription(ConfigurationSection config) {
		requirements = config.getBoolean("requirements.display");
		requirementsValid = config.getString("requirements.valid");
		requirementsInvalid = config.getString("requirements.invalid");
		
		rewards = config.getBoolean("rewards.display");
		rewardsFormat = config.getString("rewards.format");
	}
	
	public boolean showRewards() {
		return rewards;
	}
	
	public String getRewardsFormat() {
		return rewardsFormat;
	}
	
	public boolean showRequirements() {
		return requirements;
	}
	
	public String getRequirementsValid() {
		return requirementsValid;
	}
	
	public String getRequirementsInvalid() {
		return requirementsInvalid;
	}
	
	public List<String> formatDescription(Quest quest, Player p) {
		List<String> list = new ArrayList<>();
		
		QuestDescriptionContext context = new QuestDescriptionContext(this, p);
		
		StreamSupport
			.stream(quest.spliterator(), false)
			.filter(QuestDescriptionProvider.class::isInstance)
			.map(QuestDescriptionProvider.class::cast)
			.sorted(QuestDescriptionProvider.COMPARATOR)
			.forEach(provider -> {
				List<String> description = provider.provideDescription(context);
				if (description == null || description.isEmpty()) return;
				
				if (!list.isEmpty() && provider.prefixDescriptionWithNewLine()) list.add("");
				list.addAll(description);
			});
		
		return list;
	}
	
}
