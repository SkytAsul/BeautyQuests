package fr.skytasul.quests.stages;

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
import fr.skytasul.quests.Quest;
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

public class StageManager{

	private Map<Integer, AbstractStage> stages = new LinkedHashMap<>();
	private Map<PlayerAccount, Integer> playerStage = new HashMap<>();
	
	private Quest quest;
	
	public StageManager(Quest quest){
		this.quest = quest;
	}
	
	public Quest getQuest(){
		return quest;
	}
	
	public int getStageSize(){
		return stages.size();
	}
	
	public boolean contains(PlayerAccount account){
		return playerStage.containsKey(account);
	}
	
	public void addStage(AbstractStage stage){
		Validate.notNull(stage, "Stage cannot be null !");
		stages.put(stages.size(), stage);
		stage.load();
	}
	
	public int getID(AbstractStage st){
		for (Entry<Integer, AbstractStage> en : stages.entrySet()){
			if (en.getValue() == st) return en.getKey();
		}
		return 666;
	}
	
	public LinkedList<AbstractStage> getStages(){
		LinkedList<AbstractStage> tmp = new LinkedList<>();
		for (Entry<Integer, AbstractStage> en : stages.entrySet()){
			tmp.add(en.getKey(), en.getValue());
		}
		return tmp;
	}
	
	public AbstractStage getStage(int id){
		return stages.get(id);
	}
	
	public String getDescriptionLine(PlayerAccount account, Source source){
		if (!playerStage.containsKey(account)) throw new IllegalArgumentException("Account does not have this stage launched");
		AbstractStage stage = getPlayerStage(account);
		if (stage == null) return "§efinishing";
		return stage.getDescriptionLine(account, source);
	}
	/**
	 * Where do the description request come from
	 */
	public static enum Source{
		SCOREBOARD, MENU, PLACEHOLDER, FORCESPLIT, FORCELINE;
	}
	
	public int getPlayerStageID(PlayerAccount account){
		return playerStage.get(account);
	}
	
	public AbstractStage getPlayerStage(PlayerAccount account){
		if (playerStage.get(account) == null) return null;
		return getStage(playerStage.get(account));
	}
	
	public List<PlayerAccount> getPlayersForStage(AbstractStage stage){
		List<PlayerAccount> ls = new ArrayList<>();
		int id = getID(stage);
		for (Entry<PlayerAccount, Integer> en : playerStage.entrySet()){
			if (en.getValue() == id) ls.add(en.getKey());
		}
		return ls;
	}
	
	public List<OfflinePlayer> getPlayersLaunched(){
		List<OfflinePlayer> ls = new ArrayList<>();
		for (PlayerAccount account : playerStage.keySet()){
			ls.add(account.getOfflinePlayer());
		}
		return ls;
	}
	
	public List<PlayerAccount> getAccountsLaunched(){
		return new ArrayList<>(playerStage.keySet());
	}
	
	public Map<PlayerAccount, Integer> getPlayersStage(){
		return playerStage;
	}
	
	public void setPlayersStage(Map<PlayerAccount, Integer> players){
		this.playerStage = players;
	}
	
	public boolean hasStageLaunched(PlayerAccount acc, AbstractStage stage){
		if (!playerStage.containsKey(acc)) return false;
		AbstractStage ps = getPlayerStage(acc);
		if (ps == null) return false;
		return ps.equals(stage);
	}
	
	private void removePlayerStage(PlayerAccount acc) {
		AbstractStage stage = getPlayerStage(acc);
		playerStage.remove(acc);
		if (stage != null) {
			stage.end(acc);
		}
	}
	
	public boolean remove(PlayerAccount acc, boolean forced){
		if (!playerStage.containsKey(acc)) return false;
		removePlayerStage(acc);
		if (forced) Bukkit.getPluginManager().callEvent(new PlayerStageResetEvent(acc, quest));
		return true;
	}
	
	public void next(Player p){
		if (stages.size() == 0){
			Utils.sendMessage(p, Lang.QUEST_NOSTEPS.toString());
			return;
		}
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (playerStage.containsKey(acc)){
			int last = playerStage.get(acc);
			DebugUtils.logMessage("Next stage for player " + p.getName() + ", via " + DebugUtils.stackTraces(2, 4));
			AdminMode.broadcast("Player " + p.getName() + " has finished the stage " + last + " of quest " + quest.getID());
			AbstractStage stage = getStage(last);
			BukkitRunnable run = new BukkitRunnable() {
				public void run(){
					finishStage(acc, stage);
					
					if (last + 1 == stages.size()){
						quest.finish(p);
					}else {
						setStage(acc, last + 1, true);
						if (QuestsConfiguration.sendQuestUpdateMessage()) Utils.sendMessage(p, Lang.QUEST_UPDATED.toString(), quest.getName());
						Bukkit.getPluginManager().callEvent(new NextStageEvent(p, quest, stage, getStage(last + 1)));
						Utils.playPluginSound(p.getLocation(), "ITEM_FIRECHARGE_USE", 0.5F);
						if (QuestsConfiguration.showNextParticles()) QuestsConfiguration.getParticleNext().send(p, Arrays.asList(p));
					}
				}
			};
			if (stage.hasAsyncEnd()){
				run.runTaskAsynchronously(BeautyQuests.getInstance());
			}else run.run();
		}
	}
	
	public void finishStage(PlayerAccount acc, AbstractStage stage) {
		if (acc.isCurrent()){
			stage.finish(acc.getPlayer());
		}else stage.end(acc);
		playerStage.put(acc, null);
		if (acc.isCurrent()) Utils.giveRewards(acc.getPlayer(), stage.getRewards());
	}
	
	public void setStage(PlayerAccount acc, int id, boolean launchStage){
		AbstractStage stage = stages.get(new Integer(id));
		Player p = acc.getPlayer();
		if (stage == null){
			if (p != null) Lang.ERROR_OCCURED.send(p, " noStage");
			BeautyQuests.getInstance().getLogger().severe("Error into the StageManager of quest " + quest.getName() + " : the stage " + id + " doesn't exists.");
			removePlayerStage(acc);
		}else {
			playerStage.put(acc, id);
			if (p != null && launchStage){
				stage.launch(p);
			}
			stage.start(acc);
		}
	}
	
	public void remove(){
		for (AbstractStage stage : stages.values()){
			stage.unload();
		}
		stages.clear();
		playerStage.clear();
	}
	
	public Map<String, Object> serialize(){
		Map<String, Object> map = new LinkedHashMap<>();
		
		List<Map<String, Object>> st = new ArrayList<>();
		for (AbstractStage stage : stages.values()){
			try{
				Map<String, Object> datas = stage.serialize();
				if (datas != null) st.add(datas);
			}catch (Throwable ex){
				BeautyQuests.getInstance().getLogger().severe("Error when serializing the stage " + stage.getID() + " for the quest " + quest.getName());
				ex.printStackTrace();
				BeautyQuests.savingFailure = true;
				continue;
			}
		}
		
		Map<String, Object> pl = new LinkedHashMap<>();
		for (Entry<PlayerAccount, Integer> en : playerStage.entrySet()){
			pl.put(en.getKey().getIndex(), en.getValue());
		}
		
		map.put("stages", st);
		map.put("players", pl);
		
		return map;
	}
	
	public String toString() {
		return "StageManager{stages=" + stages.size() + ",players=" + playerStage.size() + "}";
	}
	
	public static StageManager deserialize(Map<String, Object> map, Quest qu){
		StageManager sm = new StageManager(qu);
		
		List<Map<String, Object>> stages = (List<Map<String, Object>>) map.get("stages");
		Map<Integer, Integer> trueOrder = new LinkedHashMap<>();
		for(int i = 0; i < stages.size(); i++){
			trueOrder.put(i, (Integer) stages.get(i).get("order"));
		}
		
		for (int i = 0; i < stages.size(); i++){
			int t = trueOrder.get(i);
			//Bukkit.broadcastMessage(i + " : " + t + " so " + stages.get(t).get("text"));
			try{
				AbstractStage st = AbstractStage.deserialize(stages.get(t), sm);
				if (st == null){
					BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + i + " for the quest " + qu.getName() + " (stage null)");
					BeautyQuests.loadingFailure = true;
					return null;
				}
				sm.addStage(st);
			}catch (Throwable ex){
				BeautyQuests.getInstance().getLogger().severe("Error when deserializing the stage " + i + " for the quest " + qu.getName());
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
						if (acc != null) sm.setStage(acc, (int) en.getValue(), false);
					}
				}
			}.runTaskLater(BeautyQuests.getInstance(), 1L);
		}
		
		return sm;
	}
	
}