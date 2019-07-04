package fr.skytasul.quests.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;

public class BranchesManager{

	private Map<Integer, QuestBranch> branches = new LinkedHashMap<>();
	Map<PlayerAccount, Integer> playerBranch = new HashMap<>();
	
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
	
	public boolean contains(PlayerAccount account){
		return playerBranch.containsKey(account);
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
	
	public QuestBranch getPlayerBranch(PlayerAccount account){
		if (!playerBranch.containsKey(account)) return null;
		return getBranch(playerBranch.get(account));
	}
	
	public List<PlayerAccount> getPlayersForBranch(QuestBranch branch){
		List<PlayerAccount> ls = new ArrayList<>();
		int id = getID(branch);
		for (Entry<PlayerAccount, Integer> en : playerBranch.entrySet()){
			if (en.getValue() == id) ls.add(en.getKey());
		}
		return ls;
	}
	
	public List<PlayerAccount> getAccountsLaunched(){
		return new ArrayList<>(playerBranch.keySet());
	}
	
	public Map<PlayerAccount, Integer> getPlayersStage(){
		return playerBranch;
	}
	
	public void setPlayersStage(Map<PlayerAccount, Integer> players){
		this.playerBranch = players;
	}
	
	public boolean hasBranchStarted(PlayerAccount acc, QuestBranch branch){
		if (!playerBranch.containsKey(acc)) return false;
		QuestBranch br = getPlayerBranch(acc);
		if (br == null) return false;
		return br.equals(branch);
	}
	
	public void startPlayer(PlayerAccount acc){
		branches.get(0).start(acc);
	}
	
	public void remove(PlayerAccount acc) {
		if (!playerBranch.containsKey(acc)) return;
		QuestBranch branch = getPlayerBranch(acc);
		playerBranch.remove(acc);
		if (branch != null) branch.remove(acc, true);
	}
	
	public void remove(){
		for (QuestBranch branch : branches.values()){
			branch.remove();
		}
		branches.clear();
		playerBranch.clear();
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
		
		Map<String, Object> pl = new LinkedHashMap<>();
		for (Entry<PlayerAccount, Integer> en : playerBranch.entrySet()){
			pl.put(en.getKey().getIndex(), en.getValue());
		}
		
		map.put("branches", st);
		map.put("players", pl);
		
		return map;
	}
	
	public String toString() {
		return "BranchesManager{branches=" + branches.size() + ",players=" + playerBranch.size() + "}";
	}
	
	public static BranchesManager deserialize(Map<String, Object> map, Quest qu){
		BranchesManager bm = new BranchesManager(qu);
		
		if (map.containsKey("stages")){ // migration <0.16
			try{
				QuestBranch branch = new QuestBranch(bm);
				bm.addBranch(branch);
				branch.load(map);
				return bm;
			}catch (Exception ex){
				BeautyQuests.getInstance().getLogger().severe("Error when converting old stages to the branches system for the quest " + qu.getName());
				ex.printStackTrace();
				BeautyQuests.loadingFailure = true;
				return null;
			}
		}
		
		
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
		
		new BukkitRunnable() {
			public void run(){
				((Map<String, Object>) map.get("players")).forEach((accId, id) -> {
					try{
						PlayerAccount acc = PlayersManager.getByIndex(accId);
						if (acc != null) bm.playerBranch.put(acc, (int) id);
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