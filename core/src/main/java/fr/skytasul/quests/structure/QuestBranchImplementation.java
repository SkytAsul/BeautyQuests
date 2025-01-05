package fr.skytasul.quests.structure;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfigurationImplementation;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.PlayerSetStageEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.branches.EndingStage;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.utils.messaging.MessageUtils;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.utils.QuestUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;
import java.util.stream.Collectors;

public class QuestBranchImplementation implements QuestBranch {

	private final List<EndingStageImplementation> endStages = new ArrayList<>(5);
	private final List<StageControllerImplementation> regularStages = new ArrayList<>(15);

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

	private boolean isInAsyncReward(@NotNull Quester quester) {
		return regularStages.stream().anyMatch(x -> x.getStage().getRewards().isInAsyncReward(quester))
				|| endStages.stream().anyMatch(x -> x.getStage().getStage().getRewards().isInAsyncReward(quester));
	}

	@Override
	public @NotNull String getDescriptionLine(@NotNull Quester quester, @NotNull DescriptionSource source) {
		QuesterQuestData datas;
		if (!quester.hasQuestDatas(getQuest()) || (datas = quester.getQuestDatas(getQuest())).getBranch() != getId())
			throw new IllegalArgumentException("Account does not have this branch launched");
		if (isInAsyncReward(quester))
			return Lang.SCOREBOARD_ASYNC_END.toString();
		if (datas.isInEndingStages()) {
			return endStages.stream()
					.map(stage -> stage.getStage().getDescriptionLine(quester, source))
					.filter(Objects::nonNull)
					.collect(Collectors.joining("{nl}" + Lang.SCOREBOARD_BETWEEN_BRANCHES + " {nl}"));
		}
		if (datas.getStage() < 0)
			return "§cerror: no stage set for branch " + getId();
		if (datas.getStage() >= regularStages.size()) return "§cerror: datas do not match";

		String descriptionLine = regularStages.get(datas.getStage()).getDescriptionLine(quester, source);
		return MessageUtils.format(QuestsConfiguration.getConfig().getStageDescriptionConfig().getStageDescriptionFormat(),
				PlaceholderRegistry.of("stage_index", datas.getStage() + 1, "stage_amount", regularStages.size(),
						"stage_description", descriptionLine == null ? "" : descriptionLine));
	}

	@Override
	public boolean hasStageLaunched(@Nullable Quester quester, @NotNull StageController stage) {
		if (quester == null)
			return false;

		if (isInAsyncReward(quester))
			return false;
		if (!quester.hasQuestDatas(getQuest()))
			return false;

		QuesterQuestData datas = quester.getQuestDatas(getQuest());
		if (datas.getBranch() != getId())
			return false;

		if (datas.isInEndingStages())
			return isEndingStage(stage);

		return getRegularStageId(stage) == datas.getStage();
	}

	public void remove(@NotNull Quester acc, boolean end) {
		if (!acc.hasQuestDatas(getQuest())) return;
		QuesterQuestData datas = acc.getQuestDatas(getQuest());
		if (end) {
			if (datas.isInEndingStages()) {
				endStages.forEach(x -> x.getStage().end(acc));
			} else if (datas.getStage() >= 0 && datas.getStage() < regularStages.size())
				getRegularStage(datas.getStage()).end(acc);
		}
		datas.setBranch(-1);
		datas.setStage(-1);
	}

	public void start(@NotNull Quester acc) {
		acc.getQuestDatas(getQuest()).setBranch(getId());
		if (!regularStages.isEmpty()){
			setPlayerStage(acc, regularStages.get(0));
		}else {
			setPlayerEndingStages(acc);
		}
	}

	@Override
	public void finishPlayerStage(@NotNull Quester quester, @NotNull StageController stage) {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Next stage for {} (coming from {})", quester.debugName(),
				stage.toString());
		QuesterQuestData datas = quester.getQuestDatas(getQuest());
		if (datas.getBranch() != getId() || (datas.isInEndingStages() && !isEndingStage(stage))
				|| (!datas.isInEndingStages() && datas.getStage() != getRegularStageId(stage))) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warningArgs(
					"Trying to finish stage {} for {} but was not in progress.",
					stage.toString(), quester.debugName());
			return;
		}

		AdminMode.broadcast("Quester " + quester.getName() + " has finished the stage " + stage.getFlowId() + " of quest "
				+ getQuest().getId());
		datas.addQuestFlow(stage);
		if (isEndingStage(stage)) { // ending stage
			for (EndingStageImplementation end : endStages) {
				if (end.getStage() != stage)
					end.getStage().end(quester);
			}
		}
		datas.setStage(-1);
		endStage(quester, (StageControllerImplementation<?>) stage, () -> {
			if (!manager.getQuest().hasStarted(quester))
				return;
			if (regularStages.contains(stage)){ // not ending stage - continue the branch or finish the quest
				int newId = getRegularStageId(stage) + 1;
				if (newId == regularStages.size()){
					if (endStages.isEmpty()){
						remove(quester, false);
						getQuest().finish(quester);
						return;
					}
					setPlayerEndingStages(quester);
				}else {
					setPlayerStage(quester, regularStages.get(newId));
				}
			}else { // ending stage - redirect to other branch
				remove(quester, false);
				QuestBranchImplementation branch = getLinkedBranch(stage);
				if (branch == null){
					getQuest().finish(quester);
					return;
				}
				branch.start(quester);
			}
			manager.questUpdated(quester);
		});
	}

	private void endStage(@NotNull Quester quester, @NotNull StageControllerImplementation<?> stage,
			@NotNull Runnable runAfter) {
		stage.end(quester);
		stage.getStage().getValidationRequirements().stream().filter(Actionnable.class::isInstance)
				.map(Actionnable.class::cast).forEach(x -> quester.getOnlinePlayers().forEach(x::trigger));

		stage.getStage().getRewards().giveRewards(quester)
				.whenComplete(BeautyQuests.getInstance().getLoggerExpanded().logError(rewardsResult -> {
					if (rewardsResult.branchInterruption()) {
						QuestsPlugin.getPlugin().getLoggerExpanded().debug("Interrupted branching in async stage end for {}",
								quester.getNameAndID());
						return;
					}

					if (QuestsConfiguration.getConfig().getQuestsConfig().stageEndRewardsMessage())
						rewardsResult.earnings()
								.forEach((player, earnings) -> Lang.FINISHED_OBTAIN.quickSend(player, "rewards",
										MessageUtils.itemsToFormattedString(earnings.toArray(new String[0]))));

					runAfter.run();
				}, "failed to give rewards", quester));
	}

	@Override
	public void setPlayerStage(@NotNull Quester quester, @NotNull StageController stage) {
		QuesterQuestData questDatas = quester.getQuestDatas(getQuest());
		if (questDatas.getBranch() != getId())
			throw new IllegalStateException("The player is not in the right branch");

		if (QuestsConfiguration.getConfig().getQuestsConfig().playerQuestUpdateMessage() && questDatas.getStage() != -1)
			Lang.QUEST_UPDATED.send(quester, getQuest());
		questDatas.setStage(getRegularStageId(stage));
		quester.getOnlinePlayers().forEach(this::playNextStage);
		((StageControllerImplementation<?>) stage).start(quester);
		Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(quester, getQuest(), stage));
	}

	@Override
	public void setPlayerEndingStages(@NotNull Quester quester) {
		QuesterQuestData datas = quester.getQuestDatas(getQuest());
		if (datas.getBranch() != getId())
			throw new IllegalStateException("The player is not in the right branch");

		if (QuestsConfiguration.getConfig().getQuestsConfig().playerQuestUpdateMessage())
			Lang.QUEST_UPDATED.send(quester, getQuest());
		datas.setInEndingStages();
		for (EndingStageImplementation endStage : endStages) {
			endStage.getStage().start(quester);
			Bukkit.getPluginManager().callEvent(new PlayerSetStageEvent(quester, getQuest(), endStage.getStage()));
		}
		quester.getOnlinePlayers().forEach(this::playNextStage);
	}

	private void playNextStage(@NotNull Player p) {
		QuestUtils.playPluginSound(p.getLocation(), QuestsConfiguration.getConfig().getQuestsConfig().nextStageSound(),
				0.5F);
		if (QuestsConfigurationImplementation.getConfiguration().showNextParticles())
			QuestsConfigurationImplementation.getConfiguration().getParticleNext().send(p, Arrays.asList(p));
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
				QuestsPlugin.getPlugin().notifySavingFailure();
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
				QuestsPlugin.getPlugin().notifySavingFailure();
			}
		}
	}

	@Override
	public String toString() {
		return "QuestBranch{regularStages=" + regularStages.size() + ",endingStages=" + endStages.size() + "}";
	}

	public boolean load(@NotNull ConfigurationSection section) {
		ConfigurationSection stagesSection;
		if (section.isList("stages")) { // TODO migration 0.19.3
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
		if (section.isList("endingStages")) { // TODO migration 0.19.3
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