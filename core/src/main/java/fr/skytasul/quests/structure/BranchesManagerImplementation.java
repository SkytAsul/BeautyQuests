package fr.skytasul.quests.structure;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterQuestData;
import fr.skytasul.quests.api.quests.branches.QuestBranch;
import fr.skytasul.quests.api.quests.branches.QuestBranchesManager;
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
		if (!acc.getDataHolder().hasQuestDatas(quest))
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

	public void save(@NotNull ConfigurationSection section) {
		ConfigurationSection branchesSection = section.createSection("branches");
		branches.forEach((id, branch) -> {
			try {
				branch.save(branchesSection.createSection(Integer.toString(id)));
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Error when serializing the branch " + id + " for the quest " + quest.getId(), ex);
				QuestsPlugin.getPlugin().notifySavingFailure();
			}
		});
	}

	@Override
	public String toString() {
		return "BranchesManager{branches=" + branches.size() + "}";
	}

	public static @NotNull BranchesManagerImplementation deserialize(@NotNull ConfigurationSection section, @NotNull QuestImplementation qu) {
		BranchesManagerImplementation bm = new BranchesManagerImplementation(qu);

		ConfigurationSection branchesSection;
		if (section.isList("branches")) { // TODO migration 0.19.3
			List<Map<?, ?>> branches = section.getMapList("branches");
			section.set("branches", null);
			branchesSection = section.createSection("branches");
			branches.stream()
					.sorted((x, y) -> {
						int xid = (Integer) x.get("order");
						int yid = (Integer) y.get("order");
						if (xid < yid) return -1;
						if (xid > yid) return 1;
						throw new IllegalArgumentException("Two branches with same order " + xid);
					}).forEach(branch -> {
						int order = (Integer) branch.remove("order");
						branchesSection.createSection(Integer.toString(order), branch);
					});
		}else {
			branchesSection = section.getConfigurationSection("branches");
		}

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
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("Cannot parse branch ID " + key + " for quest " + qu.getId());
				QuestsPlugin.getPlugin().notifyLoadingFailure();
				return null;
			}
		}

		for (QuestBranchImplementation branch : tmpBranches.keySet()) {
			try {
				if (!branch.load(tmpBranches.get(branch))) {
					QuestsPlugin.getPlugin().getLoggerExpanded().severe("Error when deserializing the branch "
							+ branch.getId() + " for the quest " + qu.getId() + " (false return)");
					QuestsPlugin.getPlugin().notifyLoadingFailure();
					return null;
				}
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded().severe(
						"Error when deserializing the branch " + branch.getId() + " for the quest " + qu.getId(), ex);
				QuestsPlugin.getPlugin().notifyLoadingFailure();
				return null;
			}
		}

		return bm;
	}

}