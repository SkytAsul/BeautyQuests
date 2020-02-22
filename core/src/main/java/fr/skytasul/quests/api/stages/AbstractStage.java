package fr.skytasul.quests.api.stages;

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
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

/**
 * <h1> Do not forget to create the <i>deserialize</i> method:</h1>
 * <code>public static AbstractStage deserialize(Map&#60;String, Object&#62; map, StageManager manager)</code>
 * @author SkytAsul
 */
public abstract class AbstractStage implements Listener{
	
	private StageType type = null;
	protected boolean asyncEnd = false;
	
	protected final QuestBranch branch;
	
	private String startMessage = null;
	private List<AbstractReward> rewards = new ArrayList<>();
	private String customText = null;
	
	public AbstractStage(QuestBranch branch){
		this.branch = branch;
		
		for (StageType type : StageType.types){
			if (type.stageClass == getClass()) this.type = type;
		}
		
		Bukkit.getPluginManager().registerEvents(this, BeautyQuests.getInstance());
	}
	
	public QuestBranch getQuestBranch(){
		return branch;
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
		return branch.getID(this);
	}
	
	public int getStoredID(){
		if (branch.isRegularStage(this)) {
			return 0;
		}
		int index = 0;
		for (AbstractStage stage : branch.getEndingStages().keySet()) {
			if (stage == this) break;
			index++;
		}
		return index;
	}

	/**
	 * Called internally when a player finish stage's objectives
	 * @param p Player who finish the stage
	 * @see BranchesManager#next(Player)
	 */
	protected final void finishStage(Player p) {
		branch.finishStage(p, this);
	}
	
	/**
	 * Called internally to test if a player has the stage started
	 * @param p Player to test
	 * @see BranchesManager#hasStageLaunched(PlayerAccount, AbstractStage)
	 */
	protected final boolean hasStarted(Player p){
		return branch.hasStageLaunched(PlayersManager.getPlayerAccount(p), this);
	}
	
	/**
	 * Called when the stage starts (player can be offline)
	 * @param acc PlayerAccount for which the stage starts
	 */
	public void start(PlayerAccount acc) {
		if (acc.isCurrent()) {
			Player p = acc.getPlayer();
			if (startMessage != null){
				if (startMessage.length() > 0){
					if (branch.getID(this) == 0){
						Lang.NpcText.sendWP(p, branch.getQuest().getStarter().getName(), startMessage, 1, 1);
					}else {
						Utils.sendOffMessage(p, startMessage);
					}
				}
			}
		}
		Map<String, Object> datas = new HashMap<>();
		initPlayerDatas(acc, datas);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), datas);
	}
	
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {}

	/**
	 * Called when the stage ends (player can be offline)
	 * @param acc PlayerAccount for which the stage ends
	 */
	public void end(PlayerAccount acc) {
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), null);
	}
	
	public final String getDescriptionLine(PlayerAccount acc, Source source){
		if (customText != null) return "§e" + Utils.format(customText, descriptionFormat(acc, source));
		try{
			return descriptionLine(acc, source);
		}catch (Exception ex){
			ex.printStackTrace();
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
	
	public void updateObjective(PlayerAccount acc, Player p, String dataKey, Object dataValue) {
		Map<String, Object> datas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStoredID());
		datas.put(dataKey, dataValue);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), datas);
		branch.getBranchesManager().objectiveUpdated(p);
	}

	protected <T> T getData(PlayerAccount acc, String dataKey) {
		Map<String, Object> stageDatas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStoredID());
		return stageDatas == null ? null : (T) stageDatas.get(dataKey);
	}

	@Deprecated // for migration only, TODO remove
	protected void setData(PlayerAccount acc, String dataKey, Object dataValue) {
		Map<String, Object> datas = acc.getQuestDatas(branch.getQuest()).getStageDatas(getStoredID());
		if (datas == null) datas = new HashMap<>();
		datas.put(dataKey, dataValue);
		acc.getQuestDatas(branch.getQuest()).setStageDatas(getStoredID(), datas);
	}

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
		
		if (branch.isRegularStage(this)) map.put("order", branch.getID(this));
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
	
	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch) throws ReflectiveOperationException{
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
			Method m = type.stageClass.getMethod("deserialize", Map.class, QuestBranch.class);
			AbstractStage st = (AbstractStage) m.invoke(null, map, branch);
			if (map.containsKey("text")) st.startMessage = (String) map.get("text");
			if (map.containsKey("customText")) st.customText = (String) map.get("customText");
			for (Map<String, Object> rew : (List<Map<String, Object>>) map.get("rewards")){
				try {
					AbstractReward reward = AbstractReward.deserialize(rew, branch.getQuest());
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
