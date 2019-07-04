package fr.skytasul.quests.api.requirements;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import fr.skytasul.quests.Quest;

public abstract class AbstractRequirement {

	public final String name;
	protected Quest quest;
	
	protected AbstractRequirement(String name){
		this.name = name;
	}
	
	/**
	 * Called when the plugin has to check if a player can start a quest with this requirement
	 * @param p Player to test
	 * @return if the player fills conditions of this requirement
	 */
	public abstract boolean test(Player p);
	
	/**
	 * Called if the condition if not filled and if the plugin allows to send a message to the player
	 * @param p Player to send the reason
	 */
	public void sendReason(Player p) {}
	
	protected abstract void save(Map<String, Object> datas);
	protected abstract void load(Map<String, Object> savedDatas);
	
	/**
	 * Called when the requirement is unloaded
	 */
	public void unload(){}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("class", getClass().getName());
		
		return map;
	}
	
	public static AbstractRequirement deserialize(Map<String, Object> map, Quest quest) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		Validate.notNull(quest, "Quest cannot be null");
		AbstractRequirement req = (AbstractRequirement) Class.forName((String) map.get("class")).newInstance();
		req.quest = quest;
		req.load(map);
		return req;
	}
	
}
