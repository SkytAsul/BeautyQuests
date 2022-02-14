package fr.skytasul.quests.api.rewards;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;

public abstract class AbstractReward extends QuestObject {

	protected AbstractReward() {
		super(QuestsAPI.getRewards());
	}
	
	@Override
	public RewardCreator getCreator() {
		return (RewardCreator) super.getCreator();
	}
	
	/**
	 * Called when the reward should be given to the player
	 * @param p Player to give the reward
	 * @return title of all the subsequent reward (for instance : "4 gold")
	 */
	public abstract List<String> give(Player p);
	
	@Override
	public abstract AbstractReward clone();
	
	public static AbstractReward deserialize(Map<String, Object> map) {
		return QuestObject.deserialize(map, QuestsAPI.getRewards());
	}
	
	public boolean isAsync() {
		return false;
	}
	
}
