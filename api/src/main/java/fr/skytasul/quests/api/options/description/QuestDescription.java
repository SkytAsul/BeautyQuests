package fr.skytasul.quests.api.options.description;

public interface QuestDescription {
	
	public boolean showRewards();
	
	public String getRewardsFormat();
	
	public boolean showRequirements();
	
	public String getRequirementsValid();
	
	public String getRequirementsInvalid();
	
}
