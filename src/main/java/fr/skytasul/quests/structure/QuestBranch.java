package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.NextStageEvent;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.Pair;

public class QuestBranch {
	
	/**
	 * Stage -> Branch and Branch Stage ID
	 */
	public Map<AbstractStage, Pair<QuestBranch, Integer>> endStages = new HashMap<>();
	private LinkedList<AbstractStage> regularStages = new LinkedList<>();
	
	private Map<PlayerAccount, PlayerAdvancement> playerAdvancement = new HashMap<>();
	
	private Quest quest;
	private BranchesManager manager;
	
	public QuestBranch(Quest quest, BranchesManager manager){
		this.quest = quest;
		this.manager = manager;
	}
	
	public Quest getQuest(){
		return quest;
	}
	
	public BranchesManager getBranchesManager(){
		return manager;
	}
	
	public int getStageSize(){
		return regularStages.size();
	}
	
	public boolean contains(PlayerAccount account){
		return playerAdvancement.containsKey(account);
	}
	
	public void addStage(AbstractStage stage){
		Validate.notNull(stage, "Stage cannot be null !");
		regularStages.add(stage);
		stage.load();
	}
	
	public int getID(AbstractStage stage){
		return regularStages.indexOf(stage);
	}
	
	public LinkedList<AbstractStage> getStages(){
		return regularStages;
	}
	
	public AbstractStage getStage(int id){
		return regularStages.get(id);
	}
	
	public String getDescriptionLine(PlayerAccount account, Source source){
		if (!playerAdvancement.containsKey(account)) throw new IllegalArgumentException("Account does not have this stage launched");
		AbstractStage stage = getPlayerStage(account);
		if (stage == null) return "§efinishing";
		return stage.getDescriptionLine(account, source);
	}
	/**
	 * Where do the description request come from
	 */
	public static enum Source{
		SCOREBOARD, MENU, PLACEHOLDER, FORCESPLIT, FORCELINE;
	}
	
	public List<OfflinePlayer> getPlayersLaunched(){
		List<OfflinePlayer> ls = new ArrayList<>();
		for (PlayerAccount account : playerAdvancement.keySet()){
			ls.add(account.getOfflinePlayer());
		}
		return ls;
	}
	
	public List<PlayerAccount> getAccountsLaunched(){
		return new ArrayList<>(playerAdvancement.keySet());
	}
	
	public Map<PlayerAccount, PlayerAdvancement> getPlayersStage(){
		return playerAdvancement;
	}
	
	public void setPlayersStage(Map<PlayerAccount, PlayerAdvancement> players){
		this.playerAdvancement = players;
	}
	
	public boolean hasStageLaunched(PlayerAccount acc, AbstractStage stage){
		PlayerAdvancement advancement = playerAdvancement.get(acc);
		if (advancement == null) return false;
		if (advancement.regularStage != -1) return stage == getStage(advancement.regularStage);
		return (endStages.keySet().contains(stage));
	}
	
	public void remove(PlayerAccount acc) {
		if (!playerAdvancement.containsKey(acc)) return;
		PlayerAdvancement advancement = playerAdvancement.get(acc);
		playerAdvancement.remove(acc);
		if (advancement.endingStages){
			endStages.keySet().forEach((x) -> x.end(acc));
		}else getStage(advancement.regularStage).end(acc);
	}
	
	public void finishStage(Player p, AbstractStage stage){
		
	}
	
	public void next(Player p){
		if (regularStages.size() == 0){
			Utils.sendMessage(p, Lang.QUEST_NOSTEPS.toString());
			return;
		}
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (playerAdvancement.containsKey(acc)){
			int last = playerAdvancement.get(acc);
			DebugUtils.logMessage("Next stage for player " + p.getName() + ", via " + DebugUtils.stackTraces(2, 4));
			AdminMode.broadcast("Player " + p.getName() + " has finished the stage " + last + " of quest " + quest.getID());
			AbstractStage stage = getStage(last);
			BukkitRunnable run = new BukkitRunnable() {
				public void run(){
					finishStage(acc, stage);
					
					if (last + 1 == regularStages.size()){ // last regular stage
						if (endStages.isEmpty()){
							quest.finish(p);
						}else {
							for (AbstractStage stage : endStages.keySet()){
								stage.launch(p);
								stage.start(acc);
							}
						}
					}else {
						setStage(acc, last + 1, true);
						if (QuestsConfiguration.sendQuestUpdateMessage()) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), quest.getName());
						Bukkit.getPluginManager().callEvent(new NextStageEvent(p, quest, stage, getStage(last + 1)));
						Utils.playPluginSound(p.getLocation(), "ITEM_FIRECHARGE_USE", 0.5F);
						if (QuestsConfiguration.showNextParticles()) QuestsConfiguration.getParticleNext().send(p, Arrays.asList(p));
					}
				}
			};
			if (stage.hasAsyncEnd()){
				run.runTaskAsynchronously(BeautyQuests.getInstance());
			}else run.run();
		}
	}
	
	public void endStage(PlayerAccount acc, AbstractStage stage) {
		playerAdvancement.put(acc, null);
		if (acc.isCurrent()){
			stage.finish(acc.getPlayer());
		}else stage.end(acc);
		if (acc.isCurrent()) Utils.giveRewards(acc.getPlayer(), stage.getRewards());
	}
	
	public void setStage(PlayerAccount acc, int id, boolean launchStage){
		AbstractStage stage = regularStages.get(id);
		Player p = acc.getPlayer();
		if (stage == null){
			if (p != null) Lang.ERROR_OCCURED.send(p, " noStage");
			BeautyQuests.getInstance().getLogger().severe("Error into the StageManager of quest " + quest.getName() + " : the stage " + id + " doesn't exists.");
			remove(acc);
		}else {
			playerAdvancement.put(acc, stage);
			if (p != null && launchStage){
				stage.launch(p);
			}
			stage.start(acc);
		}
	}
	
	public void remove(){
		for (AbstractStage stage : regularStages){
			stage.unload();
		}
		endStages.clear();
		regularStages.clear();
		playerAdvancement.clear();
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new LinkedHashMap<>();
		
		List<Map<String, Object>> st = new ArrayList<>();
		for (AbstractStage stage : regularStages){
			try{
				Map<String, Object> datas = stage.serialize();
				if (datas != null) st.add(datas);
			}catch (Throwable ex){
				BeautyQuests.getInstance().getLogger().severe("Error when serializing the stage " + stage.getID() + " for the quest " + quest.getName());
				ex.printStackTrace();
				BeautyQuests.savingFailure = true;
				continue;
			}
		}
		
		Map<String, Object> pl = new LinkedHashMap<>();
		for (Entry<PlayerAccount, Integer> en : playerAdvancement.entrySet()){
			pl.put(en.getKey().getIndex(), en.getValue());
		}
		
		map.put("stages", st);
		map.put("players", pl);
		
		return map;
	}
	
	public String toString() {
		return "StageManager{stages=" + regularStages.size() + ",players=" + playerAdvancement.size() + "}";
	}
	
	public static QuestBranch deserialize(Map<String, Object> map, BranchesManager manager){
		QuestBranch sm = new QuestBranch(manager.getQuest(), manager);
		
		List<Map<String, Object>> stages = (List<Map<String, Object>>) map.get("stages");
		Map<Integer, Integer> trueOrder = new LinkedHashMap<>();
		for(int i = 0; i < stages.size(); i++){
			trueOrder.put(i, (Integer) stages.get(i).get("order"));
		}
		
		for (int i = 0; i < stages.size(); i++){
			int t = trueOrder.get(i);
			//Bukkit.broadcastMessage(i + " : " + t + " so " + stages.get(t).get("text"));
			try{
				AbstractStage st = AbstractStage.deserialize(stages.get(t), sm);
				if (st == null){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + i + " for the quest " + manager.getQuest().getName() + " (stage null)");
					BeautyQuests.loadingFailure = true;
					return null;
				}
				sm.addStage(st);
			}catch (Throwable ex){
				BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + i + " for the quest " + manager.getQuest().getName());
				ex.printStackTrace();
				BeautyQuests.loadingFailure = true;
				return null;
			}
		}
		
		if (map.get("players") != null){
			new BukkitRunnable() {
				public void run(){
					Map<String, Object> players = (Map<String, Object>) map.get("players");
					for (Entry<String, Object> en : players.entrySet()){
						PlayerAccount acc = PlayersManager.getByIndex(en.getKey());
						if (acc != null) sm.setStage(acc, (int) en.getValue(), false);
					}
				}
			}.runTaskLater(BeautyQuests.getInstance(), 1L);
		}
		
		return sm;
	}
	
	class PlayerAdvancement{
		boolean rewards = false;
		boolean endingStages = false;
		int regularStage = 0;
		
		public void inRewards(boolean rewards){
			this.rewards = rewards;
		}
		public void inEndingStages(){
			endingStages = true;
			regularStage = -1;
		}
		public void inRegularStage(int id){
			regularStage = id;
			endingStages = false;
		}
	}
	
}