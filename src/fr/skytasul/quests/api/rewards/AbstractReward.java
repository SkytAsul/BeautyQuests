package fr.skytasul.quests.api.rewards;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;

public abstract class AbstractReward {

	protected boolean async = false;
	protected final String name;
	protected Quest quest;
	
	protected AbstractReward(String name){
		this.name = name;
	}
	
	public abstract String give(Player p);
	
	protected abstract void save(Map<String, Object> datas);
	protected abstract void load(Map<String, Object> savedDatas);
	
	public String getName(){
		return name;
	}

	public boolean isAsync(){
		return async;
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("class", getClass().getName());
		
		return map;
	}
	
	public static AbstractReward deserialize(Map<String, Object> map, Quest quest) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		AbstractReward req = (AbstractReward) Class.forName((String) map.get("class")).newInstance();
		req.quest = quest;
		req.load(map);
		return req;
	}
	
}
