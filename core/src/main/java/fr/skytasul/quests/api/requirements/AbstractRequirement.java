package fr.skytasul.quests.api.requirements;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.objects.QuestObject;
import fr.skytasul.quests.api.objects.QuestObjectCreator;
import fr.skytasul.quests.structure.Quest;

public abstract class AbstractRequirement implements QuestObject {

	private final QuestObjectCreator<? extends AbstractRequirement> creator;
	
	protected final String name;
	private Quest quest;
	
	protected AbstractRequirement(String name){
		this.name = name;
		
		this.creator = QuestsAPI.requirements.get(getClass());
		if (creator == null) throw new IllegalArgumentException(getClass().getName() + " has not been registered as a reward via the API.");
	}
	
	@Override
	public QuestObjectCreator<? extends AbstractRequirement> getCreator() {
		return creator;
	}
	
	@Override
	public String getName() {
		return name;
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
	
	@Override
	public abstract AbstractRequirement clone();
	
	protected abstract void save(Map<String, Object> datas);
	protected abstract void load(Map<String, Object> savedDatas);
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		save(map);
		map.put("class", getClass().getName());
		
		return map;
	}
	
	public static AbstractRequirement deserialize(Map<String, Object> map) {
		String className = (String) map.get("class");
		try {
			QuestObjectCreator<AbstractRequirement> creator = QuestsAPI.requirements.get(Class.forName(className));
			if (creator == null) {
				BeautyQuests.logger.severe("Cannot find requirement creator " + className);
				return null;
			}
			AbstractRequirement requirement = creator.newObjectSupplier.get();
			requirement.load(map);
			return requirement;
		}catch (ClassNotFoundException e) {
			BeautyQuests.logger.severe("Cannot find requirement class " + className);
			return null;
		}
	}
	
}
