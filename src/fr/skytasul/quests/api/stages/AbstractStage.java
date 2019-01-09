package fr.skytasul.quests.api.stages;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager;
import fr.skytasul.quests.utils.Utils;

/**
 * <b> Do not forget to create the <i>deserialize</i> method</b><br>
 * <br>
 * 
 * <code>public static AbstractStage deserialize(Map&#60;String, Object&#62; map, StageManager manager)</code>
 * @author SkytAsul
 */
public abstract class AbstractStage implements Listener{
	
	private StageType type = null;
	protected boolean asyncEnd = false;
	
	protected final StageManager manager;
	
	private String startMessage = null;
	private List<AbstractReward> rewards = new ArrayList<>();
	private String customText = null;
	
	protected AbstractStage getThis(){
		return this;
	}
	
	public AbstractStage(StageManager manager){
		this.manager = manager;
		
		for (StageType type : StageType.types){
			if (type.stageClass == getClass()) this.type = type;
		}
		
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
	}
	
	public StageManager getStageManager(){
		return manager;
	}
	
	public void setStartMessage(String text){
		this.startMessage = text;
	}
	
	public String getStartMessage(){
		return startMessage;
	}
	
	public List<AbstractReward> getRewards(){
		return rewards;
	}
	
	public void setRewards(List<AbstractReward> rewards){
		this.rewards = rewards;
		checkAsync();
	}

	public String getCustomText(){
		return customText;
	}
	
	public void setCustomText(String message) {
		this.customText = message;
	}
	
	public boolean sendStartMessage(){
		return startMessage == null && QuestsConfiguration.sendStageStartMessage();
	}
	
	public StageType getType(){
		return type;
	}
	
	public boolean hasAsyncEnd(){
		return asyncEnd;
	}
	
	public void launch(Player p){
		if (startMessage != null){
			if (startMessage.length() > 0){
				if (manager.getID(this) == 0){
					Utils.sendNPCMessage(p, startMessage, manager.getQuest().getStarter());
				}else {
					Utils.sendOffMessage(p, startMessage);
				}
			}
		}
		start(PlayersManager.getPlayerAccount(p));
	}
	
	private void checkAsync(){
		for(AbstractReward rew : rewards){
			if (rew.isAsync()) asyncEnd = true;
		}
	}
	
	public int getID(){
		return manager.getID(this);
	}
	
	protected final void finishStage(Player p) {
		manager.next(p);
	}
	
	public void start(PlayerAccount account){}
	public void end(PlayerAccount account){}
	
	public String getDescriptionLine(PlayerAccount acc, boolean menu){
		if (customText != null) return "§e" + customText;
		String s;
		try{
			if (menu) {
				s = descriptionMenu(acc);
				if (s != null) return s;
			}
			s = descriptionLine(acc);
		}catch (Throwable ex){
			s = "§a" + type.name;
		}
		return s;
	}
	
	protected abstract String descriptionLine(PlayerAccount acc);
	protected String descriptionMenu(PlayerAccount acc) {return null;}
	
	public void unload(){
        HandlerList.unregisterAll(this);
	}
	
	public void load() {}
	
	protected abstract Map<String, Object> serialize(Map<String, Object> map);
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new HashMap<>();
		
		map.put("order", manager.getID(this));
		map.put("stageType", type.id);
		map.put("text", startMessage);
		map.put("customText", customText);
		
		List<Map<String, Object>> rewls = new ArrayList<>();
		for (AbstractReward reward : rewards){
			rewls.add(reward.serialize());
		}
		map.put("rewards", rewls);
		
		return serialize(map);
		/*try{
			map = serialize(map);
		}catch (Exception ex){
			ex.printStackTrace();
			map = null;
			return map;
		}
		return map;*/
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		StageType type = StageType.getStageType((String) map.get("stageType"));
		if (type == null){
			BeautyQuests.getInstance().getLogger().warning("Unknown stage type : " + map.get("stageType"));
			return null;
		}else if (type.dependCode != null && !Bukkit.getPluginManager().isPluginEnabled((type.dependCode))){
			BeautyQuests.getInstance().getLogger().warning("The plugin " + type.dependCode + " is not enabled but needed.");
			return null;
		}
		
		try{
			Method m = type.stageClass.getMethod("deserialize", Map.class, StageManager.class);
			AbstractStage st = (AbstractStage) m.invoke(null, map, manager);
			if (map.containsKey("text")) st.startMessage = (String) map.get("text");
			if (map.containsKey("customText")) st.customText = (String) map.get("customText");
			for (Map<String, Object> rew : (List<Map<String, Object>>) map.get("rewards")){
				try {
					AbstractReward reward = AbstractReward.deserialize(rew, manager.getQuest());
					st.rewards.add(reward);
					if (reward.isAsync()) st.asyncEnd = true;
				}catch (InstantiationException | ClassNotFoundException e) {
					BeautyQuests.getInstance().getLogger().severe("Error while deserializing a reward (class " + rew.get("class") + ").");
					e.printStackTrace();
					continue;
				}
			}

			return st;
		}catch (NoSuchMethodException e){
			BeautyQuests.getInstance().getLogger().severe("No deserialize method for the class " + type.stageClass.getName() + ". Prevent SkytAsul on SpigotMC.org");
		}
		return null;
	}
}
