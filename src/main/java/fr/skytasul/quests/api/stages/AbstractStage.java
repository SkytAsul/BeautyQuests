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
import fr.skytasul.quests.stages.StageManager.Source;
import fr.skytasul.quests.utils.Utils;

/**
 * <h1> Do not forget to create the <i>deserialize</i> method:</h1>
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
	
	private void checkAsync(){
		for(AbstractReward rew : rewards){
			if (rew.isAsync()) asyncEnd = true;
		}
	}
	
	public int getID(){
		return manager.getID(this);
	}
	
	/**
	 * Called internally when a player finish stage's objectives
	 * @param p Player who finish the stage
	 * @see StageManager#next(Player)
	 */
	protected final void finishStage(Player p) {
		manager.next(p);
	}
	
	/**
	 * Called internally to test if a player has the stage started
	 * @param p Player to test
	 * @see StageManager#hasStageLaunched(PlayerAccount, AbstractStage)
	 */
	protected final boolean hasStarted(Player p){
		return manager.hasStageLaunched(PlayersManager.getPlayerAccount(p), this);
	}
	
	/**
	 * Called when the player is online at the moment the stage starts<br>
	 * {@link #start(PlayerAccount)} is called just after
	 * @param p Player who starts the stage
	 */
	public void launch(Player p){
		if (startMessage != null){
			if (startMessage.length() > 0){
				if (manager.getID(this) == 0){
					Utils.sendNPCMessage(p, startMessage, manager.getQuest().getStarter(), 1, 1);
				}else {
					Utils.sendOffMessage(p, startMessage);
				}
			}
		}
	}
	
	/**
	 * Called when the player is online at the moment the stage ends<br>
	 * {@link #end(PlayerAccount)} will be called after that
	 * @param p Player who ends the stage
	 */
	public void finish(Player p){
		end(PlayersManager.getPlayerAccount(p));
	}
	
	/**
	 * Called when the stage starts/player data is loaded (player can be offline)
	 * @param account PlayerAccount for which the stage starts
	 */
	public void start(PlayerAccount account){}
	
	/**
	 * Called when the stage ends (player can be offline)
	 * @param account PlayerAccount for which the stage ends
	 */
	public void end(PlayerAccount account){}
	
	public final String getDescriptionLine(PlayerAccount acc, Source source){
		if (customText != null) return "§e" + Utils.format(customText, descriptionFormat(acc, source));
		try{
			return descriptionLine(acc, source);
		}catch (Exception ex){
			return "§a" + type.name;
		}
	}
	
	/**
	 * @param acc PlayerAccount who has the stage in progress
	 * @param source source of the description request
	 * @return the progress of the stage for the player
	 */
	protected abstract String descriptionLine(PlayerAccount acc, Source source);
	/**
	 * Will be called only if there is a {@link #customText}
	 * @param acc PlayerAccount who has the stage in progress
	 * @param source source of the description request
	 * @return all strings that can be used to format the custom description text
	 */
	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {return null;}
	
	/**
	 * Called when the stage has to be unloaded
	 */
	public void unload(){
        HandlerList.unregisterAll(this);
        for (AbstractReward rew : rewards){
        	rew.unload();
        }
	}
	
	/**
	 * Called when the stage loads
	 */
	public void load() {}
	
	protected abstract void serialize(Map<String, Object> map);
	
	public final Map<String, Object> serialize(){
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
		
		serialize(map);
		return map;
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		StageType type = StageType.getStageType((String) map.get("stageType"));
		if (type == null){
			BeautyQuests.getInstance().getLogger().warning("Unknown stage type : " + map.get("stageType"));
			return null;
		}
		if (type.dependCode != null && !Bukkit.getPluginManager().isPluginEnabled((type.dependCode))){
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
