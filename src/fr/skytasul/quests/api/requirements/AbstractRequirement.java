package fr.skytasul.quests.api.requirements;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;

public abstract class AbstractRequirement {

	public final String name;
	protected Quest quest;
	
	protected AbstractRequirement(String name){
		this.name = name;
	}
	
	public abstract boolean test(Player p);
	
	public void sendReason(Player p) {}
	
	protected abstract void save(Map<String, Object> datas);
	protected abstract void load(Map<String, Object> savedDatas);
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("class", getClass().getName());
		
		return map;
	}
	
	public static AbstractRequirement deserialize(Map<String, Object> map, Quest quest) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		AbstractRequirement req = (AbstractRequirement) Class.forName((String) map.get("class")).newInstance();
		req.quest = quest;
		req.load(map);
		return req;
	}
	
}
