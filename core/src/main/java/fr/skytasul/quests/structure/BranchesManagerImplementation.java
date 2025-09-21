package fr.skytasul.quests.structure;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.data.DataLoadingException;
import fr.skytasul.quests.api.data.DataSavingException;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.data.QuesterQuestData;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.quests.branches.QuestBranchesManager;
import fr.skytasul.quests.api.stages.StageController;
import fr.skytasul.quests.api.stages.StageIndex;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import java.util.*;
import java.util.Map.Entry;

public class BranchesManagerImplementation implements QuestBranchesManager {

	private @NotNull Map<Integer, QuestBranchImplementation> branches = new TreeMap<>(Integer::compare);

	private final @NotNull QuestImplementation quest;

	public BranchesManagerImplementation(@NotNull QuestImplementation quest) {
		this.quest = quest;
	}

	@Override
	public @NotNull QuestImplementation getQuest() {
		return quest;
	}

	public void addBranch(@NotNull QuestBranchImplementation branch) {
		Validate.notNull(branch, "Branch cannot be null !");
		branches.put(branches.size(), branch);
	}

	@Override
	public int getId(@NotNull QuestBranch branch) {
		for (Entry<Integer, QuestBranchImplementation> en : branches.entrySet()){
			if (en.getValue() == branch) return en.getKey();
		}
		QuestsPlugin.getPlugin().getLoggerExpanded()
				.severe("Trying to get the ID of a branch not in manager of quest " + quest.getId());
		return -1;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public @UnmodifiableView @NotNull Collection<@NotNull QuestBranch> getBranches() {
		return (Collection) branches.values();
	}

	@Override
	public @Nullable QuestBranchImplementation getBranch(int id) {
		return branches.get(id);
	}

	@Override
	public @Nullable QuestBranchImplementation getPlayerBranch(@NotNull Quester acc) {
		return acc.getDataHolder().getQuestDataIfPresent(quest).map(x -> {
			if (x.getBranch().isPresent())
				return branches.get(x.getBranch().getAsInt());
			else
				return null;
		}).orElse(null);
	}

	@Override
	public boolean hasBranchStarted(@NotNull Quester acc, @NotNull QuestBranch branch) {
		return acc.getDataHolder().getQuestDataIfPresent(quest)
				.filter(x -> x.getBranch().orElse(-1) == branch.getId())
				.isPresent();
	}

	@Override
	public @Nullable StageController getStageFromIndex(@NotNull StageIndex index)
			throws IllegalArgumentException {
		// TODO convert to switch in Java 21
		if (index instanceof StageIndex.RegularStageIndex regularIndex)
			return getBranch(regularIndex.branch()).getRegularStage(regularIndex.stageIndex());
		else if (index instanceof StageIndex.EndingStageIndex endingIndex)
			return getBranch(endingIndex.branch()).getEndingStage(endingIndex.stageId());
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * Called internally when the quest is updated for the quester
	 *
	 * @param quester quester that got its quest updated
	 */
	public final void questUpdated(@NotNull Quester quester) {
		QuestsAPI.getAPI().propagateQuestsHandlers(x -> x.questUpdated(quester, quest));
	}

	public void startPlayer(@NotNull Quester acc) {
		QuesterQuestData datas = acc.getDataHolder().getQuestData(getQuest());
		datas.resetQuestFlow();
		datas.setStartingTime(OptionalLong.of(System.currentTimeMillis()));
		branches.get(0).start(acc);
	}

	public void remove(@NotNull Quester acc) {
		if (!acc.getDataHolder().hasQuestData(quest))
			return;
		QuestBranchImplementation branch = getPlayerBranch(acc);
		if (branch != null) branch.remove(acc, true);
	}

	public void remove(){
		for (QuestBranchImplementation branch : branches.values()){
			branch.remove();
		}
		branches.clear();
	}

	public void save(@NotNull ConfigurationSection section) throws DataSavingException {
		ConfigurationSection branchesSection = section.createSection("branches");
		for (Integer id : branches.keySet()) {
			var branch = branches.get(id);
			try {
				branch.save(branchesSection.createSection(Integer.toString(id)));
			} catch (DataSavingException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new DataSavingException("Error when serializing the branch " + id, ex);
			}
		}
	}

	@Override
	public String toString() {
		return "BranchesManager{branches=" + branches.size() + "}";
	}

	public static @NotNull BranchesManagerImplementation deserialize(@NotNull ConfigurationSection section,
			@NotNull QuestImplementation qu) throws DataLoadingException {
		BranchesManagerImplementation bm = new BranchesManagerImplementation(qu);

		ConfigurationSection branchesSection = section.getConfigurationSection("branches");

		// it is needed to first add all branches to branches manager
		// in order for branching stages to be able to access all branches
		// during QuestBranch#load, no matter in which order those branches are loaded
		Map<QuestBranchImplementation, ConfigurationSection> tmpBranches = new HashMap<>();
		for (String key : branchesSection.getKeys(false)) {
			try {
				int id = Integer.parseInt(key);
				QuestBranchImplementation branch = new QuestBranchImplementation(bm);
				bm.branches.put(id, branch);
				tmpBranches.put(branch, branchesSection.getConfigurationSection(key));
			}catch (NumberFormatException ex) {
				throw new DataLoadingException("Cannot parse branch ID %s for quest %d".formatted(key, qu.getId()));
			}
		}

		for (QuestBranchImplementation branch : tmpBranches.keySet()) {
			try {
				branch.load(tmpBranches.get(branch));
			} catch (DataLoadingException ex) {
				throw ex;
			} catch (Exception ex) {
				throw new DataLoadingException("Error when deserializing the branch " + branch.getId());
			}
		}

		return bm;
	}

}