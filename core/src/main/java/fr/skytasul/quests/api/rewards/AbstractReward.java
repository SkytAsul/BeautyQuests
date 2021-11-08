package fr.skytasul.quests.api.rewards;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.structure.Quest;

public abstract class AbstractReward implements QuestObject {

	private final QuestObjectCreator<? extends AbstractReward> creator;
	
	protected final String name;
	private Quest quest;
	
	protected AbstractReward(String name){
		this.name = name;
		
		this.creator = QuestsAPI.rewards.get(getClass());
		if (getCreator() == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a reward via the API.");
	}
	
	@Override
	public QuestObjectCreator<? extends AbstractReward> getCreator() {
		return creator;
	}
	
	@Override
	public String getName(){
		return name;
	}

	public boolean isAsync(){
		return false;
	}
	
	@Override
	public void attach(Quest quest) {
		this.quest = quest;
	}
	
	@Override
	public void detach() {
		this.quest = null;
	}
	
	@Override
	public Quest getAttachedQuest() {
		return quest;
	}
	
	/**
	 * Called when the reward should be given to the player
	 * @param p Player to give the reward
	 * @return title of all the subsequent reward (for instance : "4 gold")
	 */
	public abstract List<String> give(Player p);
	
	@Override
	public abstract AbstractReward clone();
	
	protected abstract void save(Map<String, Object> datas);
	
	protected abstract void load(Map<String, Object> savedDatas);
	
	public final Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("class", getClass().getName());
		
		return map;
	}
	
	public static AbstractReward deserialize(Map<String, Object> map) {
		String className = (String) map.get("class");
		try {
			QuestObjectCreator<AbstractReward> creator = QuestsAPI.rewards.get(Class.forName(className));
			if (creator == null) {
				BeautyQuests.logger.severe("Cannot find reward creator " + className);
				return null;
			}
			AbstractReward reward = creator.newObjectSupplier.get();
			reward.load(map);
			return reward;
		}catch (ClassNotFoundException e) {
			BeautyQuests.logger.severe("Cannot find reward class " + className);
			return null;
		}
	}
	
}
