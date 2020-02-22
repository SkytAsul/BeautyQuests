package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.scoreboards.Scoreboard;

public class BranchesManager{

	private Map<Integer, QuestBranch> branches = new LinkedHashMap<>();
	//Map<PlayerAccount, PlayerAdvancement> playerAdvancement = new HashMap<>();
	
	private Quest quest;
	
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
		return 666;
	}
	
	public LinkedList<QuestBranch> getBranches(){
		LinkedList<QuestBranch> tmp = new LinkedList<>();
		for (Entry<Integer, QuestBranch> en : branches.entrySet()){
			tmp.add(en.getKey(), en.getValue());
		}
		return tmp;
	}
	
	public QuestBranch getBranch(int id){
		return branches.get(id);
	}
	
	public QuestBranch getPlayerBranch(PlayerAccount acc) {
		if (!acc.hasQuestDatas(quest)) return null;
		return branches.get(acc.getQuestDatas(quest).getBranch());
	}
	
	/*public PlayerAdvancement getPlayerAdvancement(PlayerAccount account){
		return playerAdvancement.get(account);
	}*/
	
	/*public List<PlayerAccount> getPlayersForBranch(QuestBranch branch){
		List<PlayerAccount> ls = new ArrayList<>();
		int id = getID(branch);
		for (Entry<PlayerAccount, PlayerAdvancement> en : playerAdvancement.entrySet()){
			if (en.getValue().branch.getID() == id) ls.add(en.getKey());
		}
		return ls;
	}
	
	public List<PlayerAccount> getAccountsLaunched(){
		return new ArrayList<>(playerAdvancement.keySet());
	}
	
	public Map<PlayerAccount, PlayerAdvancement> getPlayersAdvancement(){
		return playerAdvancement;
	}
	
	public void setPlayersStage(Map<PlayerAccount, PlayerAdvancement> players){
		this.playerAdvancement = players;
	}*/
	
	public boolean hasBranchStarted(PlayerAccount acc, QuestBranch branch){
		if (!acc.hasQuestDatas(quest)) return false;
		return acc.getQuestDatas(quest).getBranch() == branch.getID();
	}
	
	/**
	 * Called internally when the quest is updated for the player
	 * @param p Player
	 */
	public final void objectiveUpdated(Player p) {
		Scoreboard sb = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(p);
		if (sb != null) sb.setShownQuest(quest);
	}

	public void startPlayer(PlayerAccount acc){
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
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new LinkedHashMap<>();
		
		List<Map<String, Object>> st = new ArrayList<>();
		for (QuestBranch branch : branches.values()){
			try{
				Map<String, Object> datas = branch.serialize();
				if (datas != null) st.add(datas);
			}catch (Throwable ex){
				BeautyQuests.getInstance().getLogger().severe("Error when serializing the branch " + getID(branch) + " for the quest " + quest.getName());
				ex.printStackTrace();
				BeautyQuests.savingFailure = true;
				continue;
			}
		}
		map.put("branches", st);
		
		return map;
	}
	
	public String toString() {
		return "BranchesManager{branches=" + branches.size() + "}";
	}
	
	public static BranchesManager deserialize(Map<String, Object> map, Quest qu){
		BranchesManager bm = new BranchesManager(qu);
		
		List<Map<String, Object>> branches = (List<Map<String, Object>>) map.get("branches");
		branches.sort((x, y) -> {
			int xid = (int) x.get("order");
			int yid = (int) y.get("order");
			if (xid < yid) return -1;
			if (xid > yid) return 1;
			BeautyQuests.logger.warning("Two branches with same order in quest " + qu.getID());
			return 0;
		});
		branches.forEach((x) -> bm.addBranch(new QuestBranch(bm)));

		for (int i = 0; i < branches.size(); i++) {
			try{
				if (!bm.getBranch(i).load(branches.get(i))){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the branch " + i + " for the quest " + qu.getName() + " (false return)");
					BeautyQuests.loadingFailure = true;
					return null;
				}
			}catch (Exception ex){
				BeautyQuests.getInstance().getLogger().severe("Error when deserializing the branch " + i + " for the quest " + qu.getName());
				ex.printStackTrace();
				BeautyQuests.loadingFailure = true;
				return null;
			}
		}
		
		if (!map.containsKey("playersAdvancement")) return bm; // before pre10, player datas were not saved this way
		new BukkitRunnable() {
			public void run(){
				PlayersManagerYAML managerYAML = PlayersManager.getMigrationYAML();
				((Map<String, Object>) map.get("playersAdvancement")).forEach((accId, advancement) -> {
					try{
						PlayerAccount acc = managerYAML.getByIndex(accId);
						if (acc == null) return;
						
						String adv = (String) advancement;
						int separator = adv.indexOf('|');
						QuestBranch branch = separator != -1 ? bm.getBranch(Integer.parseInt(adv.substring(0, separator))) : bm.getBranch(0);
						if (branch == null){
							BeautyQuests.getInstance().getLogger().severe("Error when deserializing player datas for the quest " + qu.getName() + ": branch is null");
							BeautyQuests.loadingFailure = true;
							return;
						}
						acc.getQuestDatas(qu).setBranch(branch.getID());
						if ("end".equals(adv.substring(separator+1))) branch.setEndingStages(acc, false); else branch.setStage(acc, Integer.parseInt(adv.substring(separator+1)));
					}catch (Exception ex){
						BeautyQuests.getInstance().getLogger().severe("Error when deserializing player datas for the quest " + qu.getName());
						ex.printStackTrace();
						BeautyQuests.loadingFailure = true;
					}
				});
			}
		}.runTaskLater(BeautyQuests.getInstance(), 1L);
		
		return bm;
	}
	
}