package fr.skytasul.quests.api.rewards;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.structure.Quest;

public abstract class AbstractReward implements Cloneable {

	private final RewardCreator<?> creator;
	
	protected boolean async = false;
	protected final String name;
	protected Quest quest;
	
	protected AbstractReward(String name){
		this.name = name;
		
		this.creator = RewardCreator.creators.get(getClass());
		if (getCreator() == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a reward via the API.");
	}
	
	public RewardCreator<?> getCreator() {
		return creator;
	}

	/**
	 * Called when the reward should be given to the player
	 * @param p Player to give the reward
	 * @return title of the reward (for instance : "4 gold")
	 */
	public abstract String give(Player p);
	
	protected abstract void save(Map<String, Object> datas);
	protected abstract void load(Map<String, Object> savedDatas);
	
	public String getName(){
		return name;
	}

	public boolean isAsync(){
		return async;
	}
	
	public void attach(Quest quest) {
		this.quest = quest;
	}
	
	public void detach() {}
	
	@Override
	public abstract AbstractReward clone();
	
	public ItemStack getItemStack() {
		return creator.item.clone();
	}
	
	public abstract void itemClick(Player p, RewardsGUI gui, ItemStack clicked);
	
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
