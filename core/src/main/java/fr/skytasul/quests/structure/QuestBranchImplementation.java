package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.quests.branches.EndingStage;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.rewards.InterruptingBranchException;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.MessageUtils;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.QuestUtils;

public class QuestBranchImplementation implements QuestBranch {
	
	private final List<EndingStageImplementation> endStages = new ArrayList<>(5);
	private final List<StageControllerImplementation> regularStages = new ArrayList<>(15);
	
	private final List<PlayerAccount> asyncReward = new ArrayList<>(5);

	private final @NotNull BranchesManagerImplementation manager;
	
	public QuestBranchImplementation(@NotNull BranchesManagerImplementation manager) {
		this.manager = manager;
	}
	
	@Override
	public @NotNull QuestImplementation getQuest() {
		return manager.getQuest();
	}
	
	@Override
	public @NotNull BranchesManagerImplementation getManager() {
		return manager;
	}
	
	public int getStageSize(){
		return regularStages.size();
	}
	
	@Override
	public int getId() {
		return manager.getId(this);
	}
	
	public void addRegularStage(@NotNull StageControllerImplementation<?> stage) {
		Validate.notNull(stage, "Stage cannot be null !");
		regularStages.add(stage);
		stage.load();
	}
	
	public void addEndStage(@NotNull StageControllerImplementation<?> stage, @NotNull QuestBranchImplementation linked) {
		Validate.notNull(stage, "Stage cannot be null !");
		endStages.add(new EndingStageImplementation(stage, linked));
		stage.load();
	}
	
	@Override
	public @NotNull @UnmodifiableView List<@NotNull StageController> getRegularStages() {
		return (List) regularStages;
	}
	
	@Override
	public @NotNull StageControllerImplementation<?> getRegularStage(int id) {
		return regularStages.get(id);
	}

	@Override
	public @NotNull @UnmodifiableView List<EndingStage> getEndingStages() {
		return (List) endStages;
	}
	
	@Override
	public @NotNull StageController getEndingStage(int id) {
		return endStages.get(id).getStage(); // TODO beware index out of bounds
	}
	
	public @Nullable QuestBranchImplementation getLinkedBranch(@NotNull StageController endingStage) {
		return endStages.stream().filter(end -> end.getStage().equals(endingStage)).findAny().get().getBranch();
	}

	public int getRegularStageId(StageController stage) {
		return regularStages.indexOf(stage);
	}

	public int getEndingStageId(StageController stage) {
		for (int i = 0; i < endStages.size(); i++) {
			EndingStage endingStage = endStages.get(i);
			if (endingStage.getStage().equals(stage))
				return i;
		}
		return -1;
	}

	public boolean isEndingStage(StageController stage) {
		return endStages.stream().anyMatch(end -> end.getStage().equals(stage));
	}

	@Override
	public @NotNull String getDescriptionLine(@NotNull PlayerAccount acc, @NotNull DescriptionSource source) {
		PlayerQuestDatas datas;
		if (!acc.hasQuestDatas(getQuest()) || (datas = acc.getQuestDatas(getQuest())).getBranch() != getId())
			throw new IllegalArgumentException("Account does not have this branch launched");
		if (asyncReward.contains(acc)) return Lang.SCOREBOARD_ASYNC_END.toString();
		if (datas.isInEndingStages()) {
			StringBuilder stb = new StringBuilder();
			int i = 0;
			for (EndingStage ending : endStages) {
				i++;
				stb.append(ending.getStage().getDescriptionLine(acc, source));
				if (i != endStages.size()){
					stb.append("{nl}");
					stb.append(Lang.SCOREBOARD_BETWEEN_BRANCHES.toString());
					stb.append("{nl}");
				}
			}
			return stb.toString();
		}
		if (datas.getStage() < 0)
			return "§cerror: no stage set for branch " + getId();
		if (datas.getStage() >= regularStages.size()) return "§cerror: datas do not match";
		return MessageUtils.format(QuestsConfigurationImplementation.getStageDescriptionFormat(), datas.getStage() + 1,
				regularStages.size(), regularStages.get(datas.getStage()).getDescriptionLine(acc, source));
	}

	@Override
	public boolean hasStageLaunched(@Nullable PlayerAccount acc, @NotNull StageController stage) {
		if (acc == null)
			return false;

		if (asyncReward.contains(acc))
			return false;
		if (!acc.hasQuestDatas(getQuest()))
			return false;

		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (datas.getBranch() != getId())
			return false;

		if (datas.isInEndingStages())
			return isEndingStage(stage);

		return getRegularStageId(stage) == datas.getStage();
	}
	
	public void remove(@NotNull PlayerAccount acc, boolean end) {
		if (!acc.hasQuestDatas(getQuest())) return;
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (end) {
			if (datas.isInEndingStages()) {
				endStages.forEach(x -> x.getStage().end(acc));
			} else if (datas.getStage() >= 0 && datas.getStage() < regularStages.size())
				getRegularStage(datas.getStage()).end(acc);
		}
		datas.setBranch(-1);
		datas.setStage(-1);
	}
	
	public void start(@NotNull PlayerAccount acc) {
		acc.getQuestDatas(getQuest()).setBranch(getId());
		if (!regularStages.isEmpty()){
			setStage(acc, 0);
		}else {
			setEndingStages(acc, true);
		}
	}
	
	public void finishStage(@NotNull Player p, @NotNull StageControllerImplementation<?> stage) {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Next stage for player " + p.getName() + " (coming from " + stage.toString() + ") via " + DebugUtils.stackTraces(1, 3));
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		@NotNull
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		if (datas.getBranch() != getId() || (datas.isInEndingStages() && !isEndingStage(stage))
				|| (!datas.isInEndingStages() && datas.getStage() != getRegularStageId(stage))) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Trying to finish stage " + stage.toString() + " for player " + p.getName() + ", but the player didn't have started it.");
			return;
		}

		AdminMode.broadcast("Player " + p.getName() + " has finished the stage " + stage.getFlowId() + " of quest "
				+ getQuest().getId());
		datas.addQuestFlow(stage);
		if (isEndingStage(stage)) { // ending stage
			for (EndingStageImplementation end : endStages) {
				if (end.getStage() != stage)
					end.getStage().end(acc);
			}
		}
		datas.setStage(-1);
		endStage(acc, stage, () -> {
			if (!manager.getQuest().hasStarted(acc)) return;
			if (regularStages.contains(stage)){ // not ending stage - continue the branch or finish the quest
				int newId = getRegularStageId(stage) + 1;
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
				QuestBranchImplementation branch = getLinkedBranch(stage);
				if (branch == null){
					getQuest().finish(p);
					return;
				}
				branch.start(acc);
			}
			manager.objectiveUpdated(p);
		});
	}
	
	private void endStage(@NotNull PlayerAccount acc, @NotNull StageControllerImplementation<?> stage,
			@NotNull Runnable runAfter) {
		if (acc.isCurrent()){
			Player p = acc.getPlayer();
			stage.end(acc);
			stage.getStage().getValidationRequirements().stream().filter(Actionnable.class::isInstance)
					.map(Actionnable.class::cast).forEach(x -> x.trigger(p));
			if (stage.getStage().hasAsyncEnd()) {
				new Thread(() -> {
					QuestsPlugin.getPlugin().getLoggerExpanded().debug("Using " + Thread.currentThread().getName() + " as the thread for async rewards.");
					asyncReward.add(acc);
					try {
						List<String> given = QuestUtils.giveRewards(p, stage.getStage().getRewards());
						if (!given.isEmpty() && QuestsConfigurationImplementation.hasStageEndRewardsMessage())
							Lang.FINISHED_OBTAIN.send(p, MessageUtils.itemsToFormattedString(given.toArray(new String[0])));
					} catch (InterruptingBranchException ex) {
						QuestsPlugin.getPlugin().getLoggerExpanded().debug(
								"Interrupted branching in async stage end for " + p.getName() + " via " + ex.toString());
						return;
					}catch (Exception e) {
						Lang.ERROR_OCCURED.send(p, "giving async rewards");
						QuestsPlugin.getPlugin().getLoggerExpanded().severe("An error occurred while giving stage async end rewards.", e);
					} finally {
						// by using the try-catch, we ensure that "asyncReward#remove" is called
						// otherwise, the player would be completely stuck
						asyncReward.remove(acc);
					}
					QuestUtils.runSync(runAfter);
				}, "BQ async stage end " + p.getName()).start();
			}else{
				try {
					List<String> given = QuestUtils.giveRewards(p, stage.getStage().getRewards());
					if (!given.isEmpty() && QuestsConfigurationImplementation.hasStageEndRewardsMessage())
						Lang.FINISHED_OBTAIN.send(p, MessageUtils.itemsToFormattedString(given.toArray(new String[0])));
					runAfter.run();
				} catch (InterruptingBranchException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded().debug(
							"Interrupted branching in async stage end for " + p.getName() + " via " + ex.toString());
				}
			}
		}else {
			stage.end(acc);
			runAfter.run();
		}
	}
	
	public void setStage(@NotNull PlayerAccount acc, int id) {
		StageControllerImplementation<?> stage = regularStages.get(id);
		Player p = acc.getPlayer();
		if (stage == null){
			if (p != null) Lang.ERROR_OCCURED.send(p, " noStage");
			QuestsPlugin.getPlugin().getLoggerExpanded().severe("Error into the StageManager of quest " + getQuest().getName() + " : the stage " + id + " doesn't exists.");
			remove(acc, true);
		}else {
			PlayerQuestDatas questDatas = acc.getQuestDatas(getQuest());
			if (QuestsConfigurationImplementation.sendQuestUpdateMessage() && p != null && questDatas.getStage() != -1)
				Lang.QUEST_UPDATED.send(p, getQuest().getName());
			questDatas.setStage(id);
			if (p != null) playNextStage(p);
			stage.start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(acc, getQuest(), stage));
		}
	}
	
	public void setEndingStages(@NotNull PlayerAccount acc, boolean launchStage) {
		Player p = acc.getPlayer();
		if (QuestsConfigurationImplementation.sendQuestUpdateMessage() && p != null && launchStage)
			Lang.QUEST_UPDATED.send(p, getQuest().getName());
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		datas.setInEndingStages();
		for (EndingStageImplementation endStage : endStages) {
			endStage.getStage().start(acc);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(acc, getQuest(), endStage.getStage()));
		}
		if (p != null && launchStage) playNextStage(p);
	}

	private void playNextStage(@NotNull Player p) {
		QuestUtils.playPluginSound(p.getLocation(), QuestsConfigurationImplementation.getNextStageSound(), 0.5F);
		if (QuestsConfigurationImplementation.showNextParticles()) QuestsConfigurationImplementation.getParticleNext().send(p, Arrays.asList(p));
	}
	
	public void remove(){
		regularStages.forEach(StageControllerImplementation::unload);
		regularStages.clear();
		endStages.forEach(end -> end.getStage().unload());
		endStages.clear();
	}
	
	public void save(@NotNull ConfigurationSection section) {
		ConfigurationSection stagesSection = section.createSection("stages");
		for (int i = 0; i < regularStages.size(); i++) {
			try {
				regularStages.get(i).getStage().save(stagesSection.createSection(Integer.toString(i)));
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Error when serializing the stage " + i + " for the quest " + getQuest().getId(), ex);
				QuestsPlugin.getPlugin().noticeSavingFailure();
			}
		}
		
		ConfigurationSection endSection = section.createSection("endingStages");
		for (int i = 0; i < endStages.size(); i++) {
			EndingStageImplementation en = endStages.get(i);
			try{
				ConfigurationSection stageSection = endSection.createSection(Integer.toString(i));
				en.getStage().getStage().save(stageSection);
				QuestBranchImplementation branchLinked = en.getBranch();
				if (branchLinked != null)
					stageSection.set("branchLinked", branchLinked.getId());
			}catch (Exception ex){
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Error when serializing the ending stage " + i + " for the quest " + getQuest().getId(), ex);
				QuestsPlugin.getPlugin().noticeSavingFailure();
			}
		}
	}
	
	@Override
	public String toString() {
		return "QuestBranch{regularStages=" + regularStages.size() + ",endingStages=" + endStages.size() + "}";
	}
	
	public boolean load(@NotNull ConfigurationSection section) {
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
				addRegularStage(StageControllerImplementation.loadFromConfig(this,
						stagesSection.getConfigurationSection(Integer.toString(id))));
			}catch (Exception ex){
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Error when deserializing the stage " + id + " for the quest " + manager.getQuest().getId(), ex);
				QuestsPlugin.getPlugin().notifyLoadingFailure();
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
					QuestBranchImplementation branchLinked = stage.contains("branchLinked") ? manager.getBranch(stage.getInt("branchLinked")) : null;
					addEndStage(StageControllerImplementation.loadFromConfig(this, stage), branchLinked);
				}catch (Exception ex){
					QuestsPlugin.getPlugin().getLoggerExpanded().severe(
							"Error when deserializing an ending stage for the quest " + manager.getQuest().getId(), ex);
					QuestsPlugin.getPlugin().notifyLoadingFailure();
					return false;
				}
			}
		}
		
		return true;
	}
	
}