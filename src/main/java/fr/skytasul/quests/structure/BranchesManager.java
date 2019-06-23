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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.NextStageEvent;
import fr.skytasul.quests.api.events.PlayerStageResetEvent;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class BranchesManager{

	private Map<Integer, QuestBranch> branches = new LinkedHashMap<>();
	private Map<PlayerAccount, Integer> playerBranch = new HashMap<>();
	
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
	
	public List<OfflinePlayer> getPlayersLaunched(){
		List<OfflinePlayer> ls = new ArrayList<>();
		for (PlayerAccount account : playerBranch.keySet()){
			ls.add(account.getOfflinePlayer());
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
		branches.get(0).setStage(acc, 0, true);
	}
	
	public void remove(PlayerAccount acc) {
		if (!playerBranch.containsKey(acc)) return;
		QuestBranch branch = getPlayerBranch(acc);
		playerBranch.remove(acc);
		if (branch != null) {
			branch.remove(acc);
		}
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
		return "StageManager{branches=" + branches.size() + ",players=" + playerBranch.size() + "}";
	}
	
	public static BranchesManager deserialize(Map<String, Object> map, Quest qu){
		BranchesManager bm = new BranchesManager(qu);
		
		List<Map<String, Object>> stages = (List<Map<String, Object>>) map.get("branches");
		Map<Integer, Integer> trueOrder = new LinkedHashMap<>();
		for(int i = 0; i < stages.size(); i++){
			trueOrder.put(i, (Integer) stages.get(i).get("order"));
		}
		
		for (int i = 0; i < stages.size(); i++){
			int t = trueOrder.get(i);
			//Bukkit.broadcastMessage(i + " : " + t + " so " + stages.get(t).get("text"));
			try{
				QuestBranch st = QuestBranch.deserialize(stages.get(t), bm);
				if (st == null){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the branch " + i + " for the quest " + qu.getName() + " (null branch)");
					BeautyQuests.loadingFailure = true;
					return null;
				}
				bm.addBranch(st);
			}catch (Throwable ex){
				BeautyQuests.getInstance().getLogger().severe("Error when deserializing the branch " + i + " for the quest " + qu.getName());
				ex.printStackTrace();
				BeautyQuests.loadingFailure = true;
				return null;
			}
		}
		
		if (map.get("players") != null){
			new BukkitRunnable() {
				public void run(){
					Map<String, Object> players = (Map<String, Object>) map.get("players");
					for (Entry<String, Object> en : players.entrySet()){
						PlayerAccount acc = PlayersManager.getByIndex(en.getKey());
						if (acc != null) bm.setStage(acc, (int) en.getValue(), false);
					}
				}
			}.runTaskLater(BeautyQuests.getInstance(), 1L);
		}
		
		return bm;
	}
	
}