package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.requirements.Actionnable;
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

	public boolean isRegularStage(AbstractStage stage){
		return regularStages.contains(stage);
	}
	
	public LinkedHashMap<AbstractStage, QuestBranch> getEndingStages() {
		return endStages;
	}
	
	public AbstractStage getEndingStage(int id) {
		int i = 0;
		for (AbstractStage stage : endStages.keySet()) {
			if (i++ == id) return stage;
		}
		return null;
	}
	
	public String getDescriptionLine(PlayerAccount acc, Source source) {
		PlayerQuestDatas datas;
		if (!acc.hasQuestDatas(getQuest()) || (datas = acc.getQuestDatas(getQuest())).getBranch() != getID()) throw new IllegalArgumentException("Account does not have this branch launched");
		if (asyncReward.contains(acc)) return Lang.SCOREBOARD_ASYNC_END.toString();
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
		if (datas.getStage() < 0) return "§cerror: no stage set for branch " + getID();
		if (datas.getStage() >= regularStages.size()) return "§cerror: datas do not match";
		return Utils.format(QuestsConfiguration.getStageDescriptionFormat(), datas.getStage() + 1, regularStages.size(), regularStages.get(datas.getStage()).getDescriptionLine(acc, source));
	}
	/**
	 * Where do the description request come from
	 */
	public static enum Source{
		SCOREBOARD, MENU, PLACEHOLDER, FORCESPLIT, FORCELINE;
	}
	
	public boolean hasStageLaunched(PlayerAccount acc, AbstractStage stage){
		if (acc == null) return false;
		if (asyncReward.contains(acc)) return false;
		if (!acc.hasQuestDatas(getQuest())) return false;
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (datas.getBranch() != getID()) return false;
		if (!datas.isInEndingStages()) return stage.getID() == datas.getStage();
		return (endStages.keySet().contains(stage));
	}
	
	public void remove(PlayerAccount acc, boolean end) {
		if (!acc.hasQuestDatas(getQuest())) return;
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (end) {
			if (datas.isInEndingStages()) {
				endStages.keySet().forEach((x) -> x.end(acc));
			}else if (datas.getStage() >= 0 && datas.getStage() < regularStages.size()) getRegularStage(datas.getStage()).end(acc);
		}
		datas.setBranch(-1);
		datas.setStage(-1);
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
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (datas.getBranch() != getID() || (datas.isInEndingStages() && isRegularStage(stage)) || (!datas.isInEndingStages() && datas.getStage() != stage.getID())) {
			BeautyQuests.logger.warning("Trying to finish stage " + stage.toString() + " for player " + p.getName() + ", but the player didn't have started it.");
			return;
		}
		AdminMode.broadcast("Player " + p.getName() + " has finished the stage " + getID(stage) + " of quest " + getQuest().getID());
		datas.addQuestFlow(stage);
		if (!isRegularStage(stage)){ // ending stage
			for (AbstractStage end : endStages.keySet()){
				if (end != stage) end.end(acc);
			}
		}
		endStage(acc, stage, () -> {
			if (!manager.getQuest().hasStarted(acc)) return;
			if (regularStages.contains(stage)){ // not ending stage - continue the branch or finish the quest
				int newId = datas.getStage() + 1;
				if (newId == regularStages.size()){
					if (endStages.isEmpty()){
						remove(acc, false);
						getQuest().finish(p);
						return;
					}
					setEndingStages(acc, true);
				}else {
					setStage(acc, newId);
				}
			}else { // ending stage - redirect to other branch
				remove(acc, false);
				QuestBranch branch = endStages.get(stage);
				if (branch == null){
					getQuest().finish(p);
					return;
				}
				branch.start(acc);
			}
			manager.objectiveUpdated(p, acc);
		});
	}
	
	public void endStage(PlayerAccount acc, AbstractStage stage, Runnable runAfter) {
		if (acc.isCurrent()){
			Player p = acc.getPlayer();
			stage.end(acc);
			stage.getValidationRequirements().stream().filter(Actionnable.class::isInstance).map(Actionnable.class::cast).forEach(x -> x.trigger(p));
			if (stage.hasAsyncEnd()){
				new Thread(() -> {
					DebugUtils.logMessage("Using " + Thread.currentThread().getName() + " as the thread for async rewards.");
					asyncReward.add(acc);
					try {
						List<String> given = Utils.giveRewards(p, stage.getRewards());
						if (!given.isEmpty() && QuestsConfiguration.hasStageEndRewardsMessage()) Lang.FINISHED_OBTAIN.send(p, Utils.itemsToFormattedString(given.toArray(new String[0])));
					}catch (Exception e) {
						Lang.ERROR_OCCURED.send(p, "giving async rewards");
						BeautyQuests.logger.severe("An error occurred while giving stage async end rewards.", e);
					}
					// by using the try-catch, we ensure that "asyncReward#remove" is called
					// otherwise, the player would be completely stuck
					asyncReward.remove(acc);
					Utils.runSync(runAfter);
				}, "BQ async stage end " + p.getName()).start();
			}else{
				List<String> given = Utils.giveRewards(p, stage.getRewards());
				if (!given.isEmpty() && QuestsConfiguration.hasStageEndRewardsMessage()) Lang.FINISHED_OBTAIN.send(p, Utils.itemsToFormattedString(given.toArray(new String[0])));
				runAfter.run();
			}
		}else {
			stage.end(acc);
			runAfter.run();
		}
	}
	
	public void setStage(PlayerAccount acc, int id) {
		AbstractStage stage = regularStages.get(id);
		Player p = acc.getPlayer();
		if (stage == null){
			if (p != null) Lang.ERROR_OCCURED.send(p, " noStage");
			BeautyQuests.getInstance().getLogger().severe("Error into the StageManager of quest " + getQuest().getName() + " : the stage " + id + " doesn't exists.");
			remove(acc, true);
		}else {
			PlayerQuestDatas questDatas = acc.getQuestDatas(getQuest());
			if (QuestsConfiguration.sendQuestUpdateMessage() && p != null && questDatas.getStage() != -1) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), getQuest().getName());
			questDatas.setStage(id);
			if (p != null) playNextStage(p);
			stage.start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(acc, getQuest(), stage));
		}
	}
	
	public void setEndingStages(PlayerAccount acc, boolean launchStage) {
		Player p = acc.getPlayer();
		if (QuestsConfiguration.sendQuestUpdateMessage() && p != null && launchStage) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), getQuest().getName());
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		datas.setInEndingStages();
		for (AbstractStage newStage : endStages.keySet()){
			newStage.start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(acc, getQuest(), newStage));
		}
		if (p != null && launchStage) playNextStage(p);
	}

	private void playNextStage(Player p) {
		Utils.playPluginSound(p.getLocation(), QuestsConfiguration.getNextStageSound(), 0.5F);
		if (QuestsConfiguration.showNextParticles()) QuestsConfiguration.getParticleNext().send(p, Arrays.asList(p));
	}
	
	public void remove(){
		regularStages.forEach(AbstractStage::unload);
		regularStages.clear();
		endStages.keySet().forEach(AbstractStage::unload);
		endStages.clear();
	}
	
	public void save(ConfigurationSection section) {
		ConfigurationSection stagesSection = section.createSection("stages");
		int id = 0;
		for (AbstractStage stage : regularStages) {
			try {
				stage.save(stagesSection.createSection(Integer.toString(id++)));
			}catch (Exception ex) {
				BeautyQuests.logger.severe("Error when serializing the stage " + stage.getID() + " for the quest " + getQuest().getID(), ex);
				BeautyQuests.savingFailure = true;
			}
		}
		
		int i = 0;
		ConfigurationSection endSection = section.createSection("endingStages");
		for (Entry<AbstractStage, QuestBranch> en : endStages.entrySet()){
			try{
				ConfigurationSection stageSection = endSection.createSection(Integer.toString(i++));
				en.getKey().save(stageSection);
				QuestBranch branchLinked = en.getValue();
				if (branchLinked != null) stageSection.set("branchLinked", branchLinked.getID());
			}catch (Exception ex){
				BeautyQuests.logger.severe("Error when serializing the ending stage " + en.getKey().getID() + " for the quest " + getQuest().getID(), ex);
				BeautyQuests.savingFailure = true;
			}
		}
	}
	
	@Override
	public String toString() {
		return "QuestBranch{regularStages=" + regularStages.size() + ",endingStages=" + endStages.size() + "}";
	}
	
	public boolean load(ConfigurationSection section) {
		ConfigurationSection stagesSection;
		if (section.isList("stages")) { // migration on 0.19.3: TODO remove
			List<Map<?, ?>> stages = section.getMapList("stages");
			section.set("stages", null);
			stagesSection = section.createSection("stages");
			stages.stream()
					.sorted((x, y) -> {
						int xid = (Integer) x.get("order");
						int yid = (Integer) y.get("order");
						if (xid < yid) return -1;
						if (xid > yid) return 1;
						throw new IllegalArgumentException("Two stages with same order " + xid);
					}).forEach(branch -> {
						int order = (Integer) branch.remove("order");
						stagesSection.createSection(Integer.toString(order), branch);
					});
		}else {
			stagesSection = section.getConfigurationSection("stages");
		}
		
		for (int id : stagesSection.getKeys(false).stream().map(Integer::parseInt).sorted().collect(Collectors.toSet())) {
			try{
				AbstractStage st = AbstractStage.deserialize(stagesSection.getConfigurationSection(Integer.toString(id)), this);
				if (st == null){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + id + " for the quest " + manager.getQuest().getID() + " (stage null)");
					BeautyQuests.loadingFailure = true;
					return false;
				}
				addRegularStage(st);
			}catch (Exception ex){
				BeautyQuests.logger.severe("Error when deserializing the stage " + id + " for the quest " + manager.getQuest().getID(), ex);
				BeautyQuests.loadingFailure = true;
				return false;
			}
		}
		
		ConfigurationSection endingStagesSection = null;
		if (section.isList("endingStages")) { // migration on 0.19.3: TODO remove
			List<Map<?, ?>> endingStages = section.getMapList("endingStages");
			section.set("endingStages", null);
			endingStagesSection = section.createSection("endingStages");
			int i = 0;
			for (Map<?, ?> stage : endingStages) {
				endingStagesSection.createSection(Integer.toString(i++), stage);
			}
		}else if (section.contains("endingStages")) {
			endingStagesSection = section.getConfigurationSection("endingStages");
		}
		
		if (endingStagesSection != null) {
			for (String key : endingStagesSection.getKeys(false)) {
				try{
					ConfigurationSection stage = endingStagesSection.getConfigurationSection(key);
					AbstractStage st = AbstractStage.deserialize(stage, this);
					if (st == null){
						BeautyQuests.getInstance().getLogger().severe("Error when deserializing an ending stage for the quest " + manager.getQuest().getID() + " (stage null)");
						BeautyQuests.loadingFailure = true;
						return false;
					}
					QuestBranch branchLinked = stage.contains("branchLinked") ? manager.getBranch(stage.getInt("branchLinked")) : null;
					addEndStage(st, branchLinked);
				}catch (Exception ex){
					BeautyQuests.logger.severe("Error when deserializing an ending stage for the quest " + manager.getQuest().getID(), ex);
					BeautyQuests.loadingFailure = true;
					return false;
				}
			}
		}
		
		return true;
	}
	
}