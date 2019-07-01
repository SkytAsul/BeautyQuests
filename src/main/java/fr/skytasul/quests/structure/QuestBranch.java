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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class QuestBranch {
	
	private Map<AbstractStage, QuestBranch> endStages = new HashMap<>();
	private LinkedList<AbstractStage> regularStages = new LinkedList<>();
	
	private Map<PlayerAccount, PlayerAdvancement> playerAdvancement = new HashMap<>();
	
	private BranchesManager manager;
	
	public QuestBranch(BranchesManager manager){
		this.manager = manager;
	}
	
	public Quest getQuest(){
		return manager.getQuest();
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
	
	public void addRegularStage(AbstractStage stage){
		Validate.notNull(stage, "Stage cannot be null !");
		regularStages.add(stage);
		stage.load();
	}
	
	public void addEndStage(AbstractStage stage, QuestBranch linked){
		Validate.notNull(stage, "Stage cannot be null !");
		endStages.put(stage, linked);
		stage.load();
	}
	
	public int getID(AbstractStage stage){
		return regularStages.indexOf(stage);
	}
	
	public LinkedList<AbstractStage> getRegularStages(){
		return regularStages;
	}
	
	public AbstractStage getRegularStage(int id){
		return regularStages.get(id);
	}

	public boolean isRegulatStage(AbstractStage stage){
		return regularStages.contains(stage);
	}
	
	public Map<AbstractStage, QuestBranch> getEndingStages(){
		return endStages;
	}
	
	public String getDescriptionLine(PlayerAccount account, Source source){
		PlayerAdvancement adv = playerAdvancement.get(account);
		if (adv == null) throw new IllegalArgumentException("Account does not have this branch launched");
		if (adv.rewards) return "§efinishing";
		if (adv.endingStages){
			StringBuilder stb = new StringBuilder();
			int i = 0;
			for (AbstractStage stage : endStages.keySet()) {
				i++;
				stb.append(stage.getDescriptionLine(account, source));
				if (i != endStages.size()) stb.append("{nl} {nl}");
			}
			return stb.toString();
		}
		return regularStages.get(adv.regularStage).getDescriptionLine(account, source);
	}
	/**
	 * Where do the description request come from
	 */
	public static enum Source{
		SCOREBOARD, MENU, PLACEHOLDER, FORCESPLIT, FORCELINE;
	}
	
	public Map<PlayerAccount, PlayerAdvancement> getPlayersStage(){
		return playerAdvancement;
	}
	
	public void setPlayersStage(Map<PlayerAccount, PlayerAdvancement> players){
		this.playerAdvancement = players;
	}
	
	public PlayerAdvancement getPlayerAdvancement(PlayerAccount acc){
		return playerAdvancement.get(acc);
	}
	
	public boolean hasStageLaunched(PlayerAccount acc, AbstractStage stage){
		PlayerAdvancement advancement = playerAdvancement.get(acc);
		if (advancement == null) return false;
		if (advancement.regularStage != -1) return stage == getRegularStage(advancement.regularStage);
		return (endStages.keySet().contains(stage));
	}
	
	public void remove(PlayerAccount acc, boolean end) {
		PlayerAdvancement advancement = playerAdvancement.remove(acc);
		manager.playerBranch.remove(acc);
		if (!end || advancement == null) return;
		if (advancement.endingStages){
			endStages.keySet().forEach((x) -> x.end(acc));
		}else getRegularStage(advancement.regularStage).end(acc);
	}
	
	public void start(PlayerAccount acc){
		manager.playerBranch.put(acc, manager.getID(this));
		if (!regularStages.isEmpty()){
			setStage(acc, 0, true);
		}else {
			setEndingStages(acc, true);
		}
	}
	
	public void finishStage(Player p, AbstractStage stage){
		DebugUtils.logMessage("Next stage for player " + p.getName() + ", via " + DebugUtils.stackTraces(2, 4));
		AdminMode.broadcast("Player " + p.getName() + " has finished the stage " + getID(stage) + " of quest " + getQuest().getID());
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (!isRegulatStage(stage)){ // ending stage
			for (AbstractStage end : endStages.keySet()){
				if (end != stage) end.finish(p);
			}
		}
		endStage(acc, stage, () -> {
			if (regularStages.contains(stage)){ // not ending stage - continue the branch or finish the quest
				int newId = playerAdvancement.get(acc).regularStage + 1;
				if (newId == regularStages.size()){
					if (endStages.isEmpty()){
						remove(acc, false);
						getQuest().finish(p);
					}else {
						setEndingStages(acc, true);
					}
				}else {
					setStage(acc, newId, true);
				}
			}else { // ending stage - redirect to other branch
				remove(acc, false);
				QuestBranch branch = endStages.get(stage);
				if (branch == null){
					getQuest().finish(p);
				}else branch.start(acc);
			}
		});
	}
	
	public void endStage(PlayerAccount acc, AbstractStage stage, Runnable runAfter) {
		if (acc.isCurrent()){
			stage.finish(acc.getPlayer());
			if (stage.hasAsyncEnd()){
				Utils.runAsync(() -> {
					PlayerAdvancement adv = playerAdvancement.get(acc);
					adv.inRewards(true);
					Utils.giveRewards(acc.getPlayer(), stage.getRewards());
					adv.inRewards(false);
					Utils.runSync(() -> runAfter.run());
				});
			}else{
				Utils.giveRewards(acc.getPlayer(), stage.getRewards());
				runAfter.run();
			}
		}else {
			stage.end(acc);
			runAfter.run();
		}
	}
	
	public void setStage(PlayerAccount acc, int id, boolean launchStage){
		AbstractStage stage = regularStages.get(id);
		Player p = acc.getPlayer();
		if (stage == null){
			if (p != null) Lang.ERROR_OCCURED.send(p, " noStage");
			BeautyQuests.getInstance().getLogger().severe("Error into the StageManager of quest " + getQuest().getName() + " : the stage " + id + " doesn't exists.");
			remove(acc, true);
		}else {
			if (playerAdvancement.containsKey(acc)){
				if (QuestsConfiguration.sendQuestUpdateMessage()) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), getQuest().getName());
				playerAdvancement.get(acc).inRegularStage(id);
			}else playerAdvancement.put(acc, new PlayerAdvancement());
			if (p != null && launchStage){
				stage.launch(p);
			}
			stage.start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(p, getQuest(), stage));
			Utils.playPluginSound(p.getLocation(), "ITEM_FIRECHARGE_USE", 0.5F);
			if (QuestsConfiguration.showNextParticles()) QuestsConfiguration.getParticleNext().send(p, Arrays.asList(p));
		}
	}
	
	public void setEndingStages(PlayerAccount acc, boolean launchStage){
		Player p = acc.getPlayer();
		if (!playerAdvancement.containsKey(acc)) playerAdvancement.put(acc, new PlayerAdvancement());
		playerAdvancement.get(acc).inEndingStages();
		for (AbstractStage newStage : endStages.keySet()){
			if (p != null && launchStage) newStage.launch(p);
			newStage.start(acc);
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
		
		map.put("order", manager.getID(this));
		
		List<Map<String, Object>> st = new ArrayList<>();
		for (AbstractStage stage : regularStages){
			try{
				Map<String, Object> datas = stage.serialize();
				if (datas != null) st.add(datas);
			}catch (Exception ex){
				BeautyQuests.getInstance().getLogger().severe("Error when serializing the stage " + stage.getID() + " for the quest " + getQuest().getName());
				ex.printStackTrace();
				BeautyQuests.savingFailure = true;
				continue;
			}
		}
		map.put("stages", st);
		
		st = new ArrayList<>();
		for (Entry<AbstractStage, QuestBranch> en : endStages.entrySet()){
			try{
				Map<String, Object> datas = en.getKey().serialize();
				if (datas != null){
					datas.put("branchLinked", manager.getID(en.getValue()));
					st.add(datas);
				}
			}catch (Exception ex){
				BeautyQuests.getInstance().getLogger().severe("Error when serializing the ending stage " + en.getKey().getID() + " for the quest " + getQuest().getName());
				ex.printStackTrace();
				BeautyQuests.savingFailure = true;
				continue;
			}
		}
		map.put("endingStages", st);
		
		Map<String, Object> pl = new LinkedHashMap<>();
		for (Entry<PlayerAccount, PlayerAdvancement> en : playerAdvancement.entrySet()){
			pl.put(en.getKey().getIndex(), en.getValue().getState());
		}
		map.put("players", pl);
		
		return map;
	}
	
	public String toString() {
		return "StageManager{stages=" + regularStages.size() + ",players=" + playerAdvancement.size() + "}";
	}
	
	public boolean load(Map<String, Object> map){
		List<Map<String, Object>> stages = (List<Map<String, Object>>) map.get("stages");
		stages.sort((x, y) -> {
			int xid = (int) x.get("order");
			int yid = (int) y.get("order");
			if (xid < yid) return -1;
			if (xid > yid) return 1;
			BeautyQuests.logger.warning("Two stages with same order in quest " + manager.getQuest().getID());
			return 0;
		});
		
		for (int i = 0; i < stages.size(); i++){
			try{
				AbstractStage st = AbstractStage.deserialize(stages.get(i), this);
				if (st == null){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + i + " for the quest " + manager.getQuest().getName() + " (stage null)");
					BeautyQuests.loadingFailure = true;
					return false;
				}
				addRegularStage(st);
			}catch (Exception ex){
				BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + i + " for the quest " + manager.getQuest().getName());
				ex.printStackTrace();
				BeautyQuests.loadingFailure = true;
				return false;
			}
		}
		
		if (map.containsKey("endingStages")){
			for (Map<String, Object> endMap : (List<Map<String, Object>>) map.get("endingStages")){
				try{
					AbstractStage st = AbstractStage.deserialize(endMap, this);
					if (st == null){
						BeautyQuests.getInstance().getLogger().severe("Error when deserializing an ending stage for the quest " + manager.getQuest().getName() + " (stage null)");
						BeautyQuests.loadingFailure = true;
						return false;
					}
					addEndStage(st, manager.getBranch((int) endMap.get("branchLinked")));
				}catch (Exception ex){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing an ending stage for the quest " + manager.getQuest().getName());
					ex.printStackTrace();
					BeautyQuests.loadingFailure = true;
					return false;
				}
			}
		}
		
		new BukkitRunnable() {
			public void run(){
				((Map<String, Object>) map.get("players")).forEach((accId, adv) -> {
					try{
						PlayerAccount acc = PlayersManager.getByIndex(accId);
						if (acc == null){
							DebugUtils.logMessage("PlayerAccount with ID " + accId + " null in quest " + manager.getQuest().getID());
							return;
						}
						playerAdvancement.put(acc, new PlayerAdvancement());
						if ("end".equals(adv)) setEndingStages(acc, false); else setStage(acc, (int) adv, false);
					}catch (Exception ex){
						BeautyQuests.getInstance().getLogger().severe("Error when deserializing player datas of " + accId + " for the branch " + map.get("order") + " in the quest " + getQuest().getName());
						ex.printStackTrace();
						BeautyQuests.loadingFailure = true;
					}
				});
			}
		}.runTaskLater(BeautyQuests.getInstance(), 1L);
		
		return true;
	}
	
	public static class PlayerAdvancement{
		boolean rewards = false;
		boolean endingStages = false;
		int regularStage = 0;
		
		public void inRewards(boolean rewards){
			this.rewards = rewards;
		}
		public boolean isInRewards(){
			return rewards;
		}
		
		public void inEndingStages(){
			endingStages = true;
			regularStage = -1;
		}
		public boolean isInEndingStages(){
			return endingStages;
		}
		public void inRegularStage(int id){
			regularStage = id;
			endingStages = false;
		}
		public int getRegularStage(){
			return regularStage;
		}
		
		public String getState(){
			return endingStages ? "end" : String.valueOf(regularStage);
		}
	}
	
}