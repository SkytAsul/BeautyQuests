package fr.skytasul.quests.api.events;

import java.util.List;

import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.rewards.AbstractReward;

/**
 * Called when a player finish a quest
 */
public class QuestFinishEvent extends PlayerQuestEvent{

	private List<AbstractReward> rewards;
	
	public QuestFinishEvent(Player who, Quest quest){
		super(who, quest);
		rewards = quest.getRewards();
	}
	
	public List<AbstractReward> getRewards(){
		return rewards;
	}

}
