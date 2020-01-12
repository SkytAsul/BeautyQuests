package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class QuestBranch {
	
	private LinkedHashMap<AbstractStage, QuestBranch> endStages = new LinkedHashMap<>();
	private LinkedList<AbstractStage> regularStages = new LinkedList<>();
	
	private List<PlayerAccount> asyncReward = new ArrayList<>(5);

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
	
	public int getID(){
		return manager.getID(this);
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
	
	public LinkedHashMap<AbstractStage, QuestBranch> getEndingStages() {
		return endStages;
	}
	
	public String getDescriptionLine(PlayerAccount acc, Source source) {
		PlayerQuestDatas datas;
		if (!acc.hasQuestDatas(getQuest()) || (datas = acc.getQuestDatas(getQuest())).getBranch() != getID()) throw new IllegalArgumentException("Account does not have this branch launched");
		if (asyncReward.contains(acc)) return "§efinishing";
		if (datas.isInEndingStages()) {
			StringBuilder stb = new StringBuilder();
			int i = 0;
			for (AbstractStage stage : endStages.keySet()) {
				i++;
				stb.append(stage.getDescriptionLine(acc, source));
				if (i != endStages.size()){
					stb.append("{nl}");
					stb.append(Lang.SCOREBOARD_BETWEEN_BRANCHES.toString());
					stb.append("{nl}");
				}
			}
			return stb.toString();
		}
		return regularStages.get(datas.getStage()).getDescriptionLine(acc, source);
	}
	/**
	 * Where do the description request come from
	 */
	public static enum Source{
		SCOREBOARD, MENU, PLACEHOLDER, FORCESPLIT, FORCELINE;
	}
	
	public boolean hasStageLaunched(PlayerAccount acc, AbstractStage stage){
		if (!acc.hasQuestDatas(getQuest())) return false;
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (datas.getBranch() != getID()) return false;
		if (!datas.isInEndingStages()) return stage == getRegularStage(datas.getStage());
		return (endStages.keySet().contains(stage));
	}
	
	public void remove(PlayerAccount acc, boolean end) {
		if (!acc.hasQuestDatas(getQuest())) return;
		PlayerQuestDatas datas = acc.removeQuestDatas(getQuest());
		if (!end) return;
		if (datas.isInEndingStages()) {
			endStages.keySet().forEach((x) -> x.end(acc));
		}else getRegularStage(datas.getStage()).end(acc);
		datas.setBranch(-1);
	}
	
	public void start(PlayerAccount acc){
		acc.getQuestDatas(getQuest()).setBranch(getID());
		if (!regularStages.isEmpty()){
			setStage(acc, 0);
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
				if (end != stage) end.end(acc);
			}
		}
		endStage(acc, stage, () -> {
			if (regularStages.contains(stage)){ // not ending stage - continue the branch or finish the quest
				int newId = acc.getQuestDatas(getQuest()).getStage() + 1;
				if (newId == regularStages.size()){
					if (endStages.isEmpty()){
						remove(acc, false);
						getQuest().finish(p);
						return;
					}else {
						setEndingStages(acc, true);
					}
				}else {
					setStage(acc, newId);
				}
			}else { // ending stage - redirect to other branch
				remove(acc, false);
				QuestBranch branch = endStages.get(stage);
				if (branch == null){
					getQuest().finish(p);
					return;
				}else branch.start(acc);
			}
			manager.objectiveUpdated(p);
		});
	}
	
	public void endStage(PlayerAccount acc, AbstractStage stage, Runnable runAfter) {
		if (acc.isCurrent()){
			stage.end(acc);
			if (stage.hasAsyncEnd()){
				Utils.runAsync(() -> {
					asyncReward.add(acc);
					Utils.giveRewards(acc.getPlayer(), stage.getRewards());
					asyncReward.remove(acc);
					Utils.runSync(runAfter);
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
	
	public void setStage(PlayerAccount acc, int id){
		AbstractStage stage = regularStages.get(id);
		Player p = acc.getPlayer();
		if (stage == null){
			if (p != null) Lang.ERROR_OCCURED.send(p, " noStage");
			BeautyQuests.getInstance().getLogger().severe("Error into the StageManager of quest " + getQuest().getName() + " : the stage " + id + " doesn't exists.");
			remove(acc, true);
		}else {
			if (QuestsConfiguration.sendQuestUpdateMessage() && p != null) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), getQuest().getName());
			acc.getQuestDatas(getQuest()).setStage(id);
			if (p != null) {
				Utils.playPluginSound(p.getLocation(), "ITEM_FIRECHARGE_USE", 0.5F);
				if (QuestsConfiguration.showNextParticles()) QuestsConfiguration.getParticleNext().send(p, Arrays.asList(p));
			}
			stage.start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(acc, getQuest(), stage));
		}
	}
	
	public void setEndingStages(PlayerAccount acc, boolean launchStage){
		Player p = acc.getPlayer();
		if (QuestsConfiguration.sendQuestUpdateMessage() && p != null && launchStage) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), getQuest().getName());
		acc.getQuestDatas(getQuest()).setInEndingStages();
		for (AbstractStage newStage : endStages.keySet()){
			newStage.start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(acc, getQuest(), newStage));
		}
		if (p != null && launchStage){
			Utils.playPluginSound(p.getLocation(), "ITEM_FIRECHARGE_USE", 0.5F);
			if (QuestsConfiguration.showNextParticles()) QuestsConfiguration.getParticleNext().send(p, Arrays.asList(p));
		}
	}
	
	public void remove(){
		for (AbstractStage stage : regularStages){
			stage.unload();
		}
		endStages.clear();
		regularStages.clear();
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
		
		return map;
	}
	
	public String toString() {
		return "QuestBranch{regularStages=" + regularStages.size() + ",endingStages=" + endStages.size() + "}";
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
		
		return true;
	}
	
}