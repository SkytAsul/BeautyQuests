package fr.skytasul.quests.api.rewards;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.structure.Quest;

public abstract class AbstractReward implements QuestObject {

	private final QuestObjectCreator<? extends AbstractReward> creator;
	
	protected boolean async = false;
	protected final String name;
	protected Quest quest;
	
	protected AbstractReward(String name){
		this.name = name;
		
		this.creator = QuestsAPI.rewards.get(getClass());
		if (getCreator() == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a reward via the API.");
	}
	
	public QuestObjectCreator<? extends AbstractReward> getCreator() {
		return creator;
	}
	
	@Override
	public String getName(){
		return name;
	}

	public boolean isAsync(){
		return async;
	}
	
	@Override
	public void attach(Quest quest) {
		this.quest = quest;
	}
	
	@Override
	public void detach() {}
	
	/**
	 * Called when the reward should be given to the player
	 * @param p Player to give the reward
	 * @return title of the reward (for instance : "4 gold")
	 */
	public abstract String give(Player p);
	
	@Override
	public abstract AbstractReward clone();
	
	@Override
	public ItemStack getItemStack() {
		return ItemUtils.lore(creator.item.clone(), getLore());
	}
	
	@Override
	public abstract void itemClick(Player p, QuestObjectGUI<? extends QuestObject> gui, ItemStack clicked);
	
	protected abstract void save(Map<String, Object> datas);
	
	protected abstract void load(Map<String, Object> savedDatas);
	
	public final Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("class", getClass().getName());
		
		return map;
	}
	
	public static AbstractReward deserialize(Map<String, Object> map, Quest quest) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		AbstractReward reward = QuestsAPI.rewards.get(Class.forName((String) map.get("class"))).newObjectSupplier.get();
		reward.load(map);
		reward.attach(quest);
		return reward;
	}
	
}
