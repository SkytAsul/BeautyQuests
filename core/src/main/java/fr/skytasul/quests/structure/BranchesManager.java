package fr.skytasul.quests.structure;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;

public class BranchesManager{

	private Map<Integer, QuestBranch> branches = new LinkedHashMap<>();
	
	private final Quest quest;
	
	public BranchesManager(Quest quest){
		this.quest = quest;
	}
	
	public Quest getQuest(){
		return quest;
	}
	
	public int getBranchesAmount(){
		return branches.size();
	}
	
	public void addBranch(QuestBranch branch){
		Validate.notNull(branch, "Branch cannot be null !");
		branches.put(branches.size(), branch);
	}
	
	public int getID(QuestBranch branch){
		for (Entry<Integer, QuestBranch> en : branches.entrySet()){
			if (en.getValue() == branch) return en.getKey();
		}
		BeautyQuests.logger.severe("Trying to get the ID of a branch not in manager of quest " + quest.getID());
		return -1;
	}
	
	public List<QuestBranch> getBranches() {
		return branches.entrySet().stream().sorted(Comparator.comparingInt(Entry::getKey)).map(Entry::getValue).collect(Collectors.toList());
	}
	
	public QuestBranch getBranch(int id){
		return branches.get(id);
	}
	
	public QuestBranch getPlayerBranch(PlayerAccount acc) {
		if (!acc.hasQuestDatas(quest)) return null;
		return branches.get(acc.getQuestDatas(quest).getBranch());
	}
	
	public boolean hasBranchStarted(PlayerAccount acc, QuestBranch branch){
		if (!acc.hasQuestDatas(quest)) return false;
		return acc.getQuestDatas(quest).getBranch() == branch.getID();
	}
	
	/**
	 * Called internally when the quest is updated for the player
	 * @param p Player
	 */
	public final void objectiveUpdated(Player p, PlayerAccount acc) {
		if (quest.hasStarted(acc)) {
			QuestsAPI.propagateQuestsHandlers(x -> x.questUpdated(acc, p, quest));
		}
	}

	public void startPlayer(PlayerAccount acc){
		PlayerQuestDatas datas = acc.getQuestDatas(getQuest());
		datas.resetQuestFlow();
		datas.setStartingTime(System.currentTimeMillis());
		branches.get(0).start(acc);
	}
	
	public void remove(PlayerAccount acc) {
		if (!acc.hasQuestDatas(quest)) return;
		QuestBranch branch = getPlayerBranch(acc);
		if (branch != null) branch.remove(acc, true);
	}
	
	public void remove(){
		for (QuestBranch branch : branches.values()){
			branch.remove();
		}
		branches.clear();
	}
	
	public void save(ConfigurationSection section) {
		ConfigurationSection branchesSection = section.createSection("branches");
		branches.forEach((id, branch) -> {
			try {
				branch.save(branchesSection.createSection(Integer.toString(id)));
			}catch (Exception ex) {
				BeautyQuests.logger.severe("Error when serializing the branch " + id + " for the quest " + quest.getID(), ex);
				BeautyQuests.savingFailure = true;
			}
		});
	}
	
	@Override
	public String toString() {
		return "BranchesManager{branches=" + branches.size() + "}";
	}
	
	public static BranchesManager deserialize(ConfigurationSection section, Quest qu) {
		BranchesManager bm = new BranchesManager(qu);
		
		ConfigurationSection branchesSection;
		if (section.isList("branches")) { // migration on 0.19.3: TODO remove
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
		Map<QuestBranch, ConfigurationSection> tmpBranches = new HashMap<>();
		for (String key : branchesSection.getKeys(false)) {
			try {
				int id = Integer.parseInt(key);
				QuestBranch branch = new QuestBranch(bm);
				bm.branches.put(id, branch);
				tmpBranches.put(branch, branchesSection.getConfigurationSection(key));
			}catch (NumberFormatException ex) {
				BeautyQuests.logger.severe("Cannot parse branch ID " + key + " for quest " + qu.getID());
				BeautyQuests.loadingFailure = true;
				return null;
			}
		}
		
		for (QuestBranch branch : tmpBranches.keySet()) {
			try {
				if (!branch.load(tmpBranches.get(branch))) {
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the branch " + branch.getID() + " for the quest " + qu.getID() + " (false return)");
					BeautyQuests.loadingFailure = true;
					return null;
				}
			}catch (Exception ex) {
				BeautyQuests.logger.severe("Error when deserializing the branch " + branch.getID() + " for the quest " + qu.getID(), ex);
				BeautyQuests.loadingFailure = true;
				return null;
			}
		}
		
		return bm;
	}
	
}