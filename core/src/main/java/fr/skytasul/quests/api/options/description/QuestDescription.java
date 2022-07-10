package fr.skytasul.quests.api.options.description;

import org.bukkit.configuration.ConfigurationSection;

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
	
}
