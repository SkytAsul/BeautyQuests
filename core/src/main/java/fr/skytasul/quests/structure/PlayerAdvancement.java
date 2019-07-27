package fr.skytasul.quests.structure;

public class PlayerAdvancement{
	QuestBranch branch;
	boolean rewards = false;
	boolean endingStages = false;
	int regularStage = 0;
	
	public PlayerAdvancement(QuestBranch branch){
		this.branch = branch;
	}
	
	public void inRewards(boolean rewards){
		this.rewards = rewards;
	}
	public boolean isInRewards(){
		return rewards;
	}
	
	public void inEndingStages(){
		endingStages = true;
		regularStage = -1;
	}
	public boolean isInEndingStages(){
		return endingStages;
	}
	public void inRegularStage(int id){
		regularStage = id;
		endingStages = false;
	}
	public int getRegularStage(){
		return regularStage;
	}
	
	public String getState(){
		return branch.getID() + "|" + (endingStages ? "end" : regularStage);
	}
}