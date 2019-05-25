package fr.skytasul.quests;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.stages.StageManager;
import fr.skytasul.quests.stages.StageNPC;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Dynmap;
import fr.skytasul.quests.utils.compatibility.HolographicDisplays;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quest{
	
	private final int id;
	final File file;
	private StageManager manager;
	
	private String name;
	private String endMessage;
	private String hologramText;
	private NPC npcStarter;
	private Dialog dialog;
	private List<AbstractRequirement> requirements = new ArrayList<>();
	private List<AbstractReward> rewards = new ArrayList<>();
	private List<AbstractReward> startRewards = new ArrayList<>();
	private boolean repeatable = false;
	private boolean scoreboard = true;
	private boolean hid = false;
	private boolean bypassLimit = false;
	private int timer = -1;
	
	private Map<Player, Integer> dgPlayers = new HashMap<>();
	private final List<PlayerAccount> finished = new ArrayList<>();
	private final Map<PlayerAccount, Long> inTimer = new HashMap<>();
	
	private boolean removed = false;
	private boolean asyncEnd = false;
	private List<Player> asyncStart = null;
	private Object holoAll;
	private Object holoLaunch;
	
	private BukkitTask holoTask;
	private boolean hologramLaunch = Dependencies.holod && QuestsConfiguration.getHoloLaunchItem() != null;
	private boolean hologramAll = Dependencies.holod && !StringUtils.isEmpty(getHologramText());
	
	List<Player> launcheable = new ArrayList<>();
	private List<Player> particles = new ArrayList<>();
	
	public Quest(String name, NPC npc, int id){
		this.name = name;
		this.manager = new StageManager(this);
		this.npcStarter = npc;
		this.id = id;
		if (id >= BeautyQuests.lastID) BeautyQuests.lastID = id;
		this.file = new File(BeautyQuests.saveFolder, id + ".yml");
	}
	
	public void create() {
		if (hologramAll || hologramLaunch){
			launchHologramTask();
		}
		if (Dependencies.dyn) Dynmap.addMarker(this);
	}
	
	void updateLauncheable(LivingEntity en) {
		if (hologramLaunch) updateHoloLaunchVisibility();
		if (QuestsConfiguration.showStartParticles()) {
			if (launcheable.isEmpty()) return;
			particles.clear();
			particles.addAll(launcheable);
			QuestsConfiguration.getParticleStart().send(en, particles);
		}
	}
	
	private void launchHologramTask(){
		if (npcStarter == null) return;
		holoTask = new BukkitRunnable() {
			public void run() {
				if (removed){
					removeHoloAll();
					removeHoloLaunch();
					this.cancel();
				}
				boolean rem = npcStarter.getEntity() == null;
				if (!rem) rem = !npcStarter.getEntity().getType().isAlive();
				if (rem){
					if (hologramAll) removeHoloAll();
					if (hologramLaunch) removeHoloLaunch();
					return;
				}
				if (hologramAll && holoAll == null) createHoloAll();
				if (hologramLaunch && holoLaunch == null) createHoloLaunch();
				if (hologramAll) HolographicDisplays.teleport(holoAll, Utils.upLocationForEntity((LivingEntity) npcStarter.getEntity(), 0));
				if (hologramLaunch) HolographicDisplays.teleport(holoLaunch, Utils.upLocationForEntity((LivingEntity) npcStarter.getEntity(), 1));
			}
		}.runTaskTimer(BeautyQuests.getInstance(), 1L, 1L);
	}
	
	private void createHoloAll(){
		for (Quest quest : QuestsAPI.getQuestsAssigneds(npcStarter)) {
			if (quest.holoAll != null) {
				hologramAll = false;
				return;
			}
		}
		holoAll = HolographicDisplays.createHologram(npcStarter.getStoredLocation(), true);
		HolographicDisplays.appendTextLine(holoAll, getHologramText());
	}
	
	private void removeHoloAll(){
		HolographicDisplays.delete(holoAll);
		holoAll = null;
	}
	
	private void createHoloLaunch(){
		if (!HolographicDisplays.hasProtocolLib()) {
			for (Quest quest : QuestsAPI.getQuestsAssigneds(npcStarter)) {
				if (quest.holoLaunch != null) {
					hologramLaunch = false;
					return;
				}
			}
		}
		holoLaunch = HolographicDisplays.createHologram(npcStarter.getStoredLocation(), false);
		HolographicDisplays.appendItem(holoLaunch, QuestsConfiguration.getHoloLaunchItem());
	}
	
	private void removeHoloLaunch(){
		HolographicDisplays.delete(holoLaunch);
		holoLaunch = null;
	}
	
	private void updateHoloLaunchVisibility(){
		try {
			HolographicDisplays.setPlayersVisible(holoLaunch, launcheable);
		}catch (IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	
	public String getName(){
		return name;
	}
	
	public StageManager getStageManager(){
		return manager;
	}
	
	public String getRawHologramText(){
		return hologramText;
	}
	
	public String getHologramText() {
		if (hologramText == null){
			if (QuestsConfiguration.isTextHologramDisabled()) return null;
			return Lang.HologramText.toString();
		}
		if (hologramText.equals("none")) return null;
		return hologramText;
	}
	
	public void setHologramText(String hologramText) {
		this.hologramText = hologramText;
	}
	
	public int getRawTimer(){
		return timer;
	}
	
	public int getTimer() {
		if (timer == -1) return QuestsConfiguration.getTimeBetween();
		return timer;
	}
	
	public void setTimer(int timer) {
		this.timer = timer;
	}
	
	public List<AbstractReward> getRewards() {
		return new ArrayList<>(rewards);
	}

	public void setRewards(List<AbstractReward> rewards) {
		this.rewards = rewards;
		for(AbstractReward rew : rewards){
			if (rew.isAsync()) {
				asyncEnd = true;
				break;
			}
		}
	}
	
	public List<AbstractReward> getStartRewards() {
		return new ArrayList<>(startRewards);
	}

	public void setStartRewards(List<AbstractReward> rewards) {
		this.startRewards = rewards;
		this.asyncStart = null;
		for(AbstractReward rew : startRewards){
			if (rew.isAsync()) {
				asyncStart = new ArrayList<>();
				break;
			}
		}
	}
	
	public Dialog getStartDialog(){
		return dialog;
	}
	
	public void setStartDialog(Dialog dialog){
		this.dialog = dialog;
	}

	public NPC getStarter() {
		return npcStarter;
	}
	
	public void setRepeatable(boolean multiple){
		this.repeatable = multiple;
	}
	
	public boolean isRepeatable(){
		return repeatable;
	}
	
	public boolean isRemoved(){
		return removed;
	}
	
	public boolean isScoreboardEnabled(){
		return scoreboard;
	}

	public void setScoreboardEnabled(boolean enableScoreboard){
		this.scoreboard = enableScoreboard;
	}
	
	public boolean canBypassLimit(){
		return bypassLimit;
	}
	
	public void setBypassLimit(boolean bypassLimit){
		this.bypassLimit = bypassLimit;
	}

	public boolean isHid(){
		return hid;
	}

	public void setHid(boolean hid){
		this.hid = hid;
	}

	public List<AbstractRequirement> getRequirements(){
		return requirements;
	}
	
	public void setEndMessage(String msg){
		this.endMessage = msg;
	}
	
	public String getEndMessage(){
		return endMessage;
	}
	
	public int getID(){
		return id;
	}
	
	public void copyFinished(Quest quest){
		finished.clear();
		finished.addAll(quest.finished);
	}
	
	public int getTimeLeft(PlayerAccount acc){
		if (!inTimer.containsKey(acc)) return -1;
		return Math.max((int) Math.ceil((inTimer.get(acc) - System.currentTimeMillis()) / 1000D / 60D), 0);
	}

	public boolean hasStarted(PlayerAccount acc){
		if (manager.contains(acc)) return true;
		if (acc.isCurrent() && asyncStart != null && asyncStart.contains(acc.getPlayer())) return true;
		return false;
	}

	public boolean hasFinished(PlayerAccount acc){
		return finished.contains(acc);
	}
	
	public boolean cancelPlayer(PlayerAccount acc){
		return manager.remove(acc, true);
	}
	
	public boolean resetPlayer(PlayerAccount acc){
		if (acc == null) return false;
		boolean c = false;
		if (manager.remove(acc, true)) c = true;
		if (finished.remove(acc)) c = true;
		if (inTimer.remove(acc) != null) c = true;
		if (acc.isCurrent() && dgPlayers.remove(acc.getPlayer()) != null) c = true;
		return c;
	}
	
	public boolean isLauncheable(Player p, boolean sendMessage){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			if (sendMessage) Lang.ALREADY_STARTED.send(p);
			return false;
		}
		if (!repeatable && finished.contains(acc)) return false;
		if (!testTimer(p, acc, sendMessage)) return false;
		if (!testRequirements(p, acc, sendMessage)) return false;
		return true;
	}
	
	public boolean testRequirements(Player p, PlayerAccount acc, boolean sendMessage){
		if (!p.hasPermission("beautyquests.start")) return false;
		if (QuestsConfiguration.getMaxLaunchedQuests() != 0 && !bypassLimit) {
			if (QuestsAPI.getStartedSize(acc) >= QuestsConfiguration.getMaxLaunchedQuests()) return false;
		}
		for (AbstractRequirement ar : requirements){
			if (!ar.test(p)) {
				if (sendMessage) ar.sendReason(p);
				return false;
			}
		}
		return true;
	}
	
	public boolean testTimer(Player p, PlayerAccount acc, boolean sendMessage){
		if (repeatable && inTimer.containsKey(acc)){
			long time = inTimer.get(acc);
			if (time > System.currentTimeMillis()){
				if (sendMessage) if (QuestsAPI.getQuestsAssigneds(npcStarter).size() == 1) Lang.QUEST_WAIT.send(p, getTimeLeft(acc));
				return false;
			}
			inTimer.remove(acc);
		}
		return true;
	}
	
	public boolean isInDialog(Player p) {
		return dgPlayers.containsKey(p);
	}
	
	public void clickNPC(Player p){
		//if (!isLauncheable(p, true)) return;
		if (sendDialog(p)) start(p);
	}
	
	private boolean sendDialog(Player p){
		if (dialog == null) return true;
		if (dialog.messages.isEmpty()) return true;
		int id;
		if (dgPlayers.containsKey(p)){ // player has already started
			id = dgPlayers.get(p);
			if (id >= dialog.messages.valuesSize()) id = 0;
			dgPlayers.remove(p); // remove from list
		}else { // never started
			id = 0;
		}
		dialog.send(p, id);
		
		id++;
		if (dialog.messages.valuesSize() == id) return true;
		// not last message
		dgPlayers.put(p, id); // add in list
		return false;
	}
	
	public void start(Player p){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			Lang.ALREADY_STARTED.send(p);
			return;
		}
		QuestLaunchEvent event = new QuestLaunchEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		AdminMode.broadcast(p.getName() + " started the quest " + id);
		launcheable.remove(p);
		inTimer.remove(acc);
		Lang.STARTED_QUEST.send(p, name);
		
		BukkitRunnable run = new BukkitRunnable() {
			public void run(){
				List<String> msg = Utils.giveRewards(p, startRewards);
				if (!msg.isEmpty()) Utils.sendMessage(p, Lang.FINISHED_OBTAIN.format(Utils.itemsToFormattedString(msg.toArray(new String[0]))));
				manager.setStage(acc, 0, true);
			}
		};
		if (asyncStart != null){
			asyncStart.add(p);
			run.runTaskAsynchronously(BeautyQuests.getInstance());
		}else run.run();
	}
	
	public void finish(Player p){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		QuestFinishEvent event = new QuestFinishEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);
		AdminMode.broadcast(p.getName() + " completed the quest " + id);
		
		BukkitRunnable run = new BukkitRunnable() {
			public void run(){
				List<String> msg = Utils.giveRewards(p, rewards);
				Utils.sendMessage(p, Lang.FINISHED_BASE.format(name) + (msg.isEmpty() ? "" : " " + Lang.FINISHED_OBTAIN.format(Utils.itemsToFormattedString(msg.toArray(new String[0])))));
				
				if (endMessage != null){
					if (manager.getStages().getLast() instanceof StageNPC){
						Utils.sendNPCMessage(p, endMessage, ((StageNPC) manager.getStages().getLast()).getNPC(), 1, 1);
					}else {
						Utils.sendOffMessage(p, endMessage);
					}
				}
				manager.remove(acc, false);
				if (!finished.contains(acc)) finished.add(acc);
				if (repeatable){
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, getTimer());
					inTimer.put(acc, cal.getTime().getTime());
				}
				Utils.spawnFirework(p.getLocation());
				Utils.playPluginSound(p, QuestsConfiguration.getFinishSound(), 1);
			}
		};
		if (asyncEnd){
			run.runTaskAsynchronously(BeautyQuests.getInstance());
		}else run.run();
	}

	public void remove(boolean msg){
		List<PlayerAccount> accounts = manager.getAccountsLaunched();
		
		BeautyQuests.getInstance().removeQuest(this);
		unloadAll();
		if (file.exists()) file.delete();
		removed = true;
		Bukkit.getPluginManager().callEvent(new QuestRemoveEvent(this, accounts));
		if (msg) BeautyQuests.getInstance().getLogger().info("The quest \"" + name + "\" has been removed");
	}
	
	public void unloadAll(){
		manager.remove();
        if (holoTask != null) holoTask.cancel();
		if (holoAll != null) removeHoloAll();
        if (holoLaunch != null) removeHoloLaunch();
		if (Dependencies.dyn) Dynmap.removeMarker(this);
		for (AbstractReward rew : rewards){
			rew.unload();
		}
		for (AbstractRequirement req : requirements){
			req.unload();
		}
	}
	
	public String toString(){
		return "Quest{npcID=" + npcStarter.getId() + ",stages=" + manager.toString() + ",several=" + repeatable + "}";
	}
	
	public Map<String, Object> serialize() throws Exception{
		Map<String, Object> map = new HashMap<>();

		map.put("name", name);
		map.put("id", id);
		map.put("manager", manager.serialize());
		map.put("starterID", npcStarter.getId());
		if (repeatable) map.put("repeatable", repeatable);
		map.put("scoreboard", scoreboard);
		if (hologramText != null) map.put("hologramText", hologramText);
		if (hid) map.put("hid", true);
		if (endMessage != null) map.put("endMessage", endMessage);
		if (dialog != null) map.put("startDialog", dialog.serialize());
		if (bypassLimit) map.put("bypassLimit", bypassLimit);
		if (timer > -1) map.put("timer", timer);
		
		List<String> list = new ArrayList<>();
		for (PlayerAccount account : finished){
			list.add(account.getIndex());
		}
		map.put("finished", list);
		
		if (!inTimer.isEmpty()){
			Map<String, String> tmap = new HashMap<>();
			for (Entry<PlayerAccount, Long> en : inTimer.entrySet()){
				tmap.put(en.getKey().getIndex(), Utils.getDateFormat().format(new Date(en.getValue())));
			}
			map.put("inTimer", tmap);
		}
		
		List<Map<String, Object>> rlist = new ArrayList<>();
		for (AbstractRequirement req : requirements){
			rlist.add(req.serialize());
		}
		map.put("requirements", rlist);
		
		List<Map<String, Object>> rewls = new ArrayList<>();
		for (AbstractReward reward : rewards){
			rewls.add(reward.serialize());
		}
		map.put("rewardsList", rewls);
		
		List<Map<String, Object>> srewls = new ArrayList<>();
		for (AbstractReward reward : startRewards){
			srewls.add(reward.serialize());
		}
		map.put("startRewardsList", srewls);
		
		return map;
	}
	

	public static Quest loadFromFile(File file){
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			return deserialize((Map<String, Object>) config.getMapList("quest").get(0));
		}catch (Throwable e) {
			BeautyQuests.logger.warning("Error when loading quests from data file.");
			e.printStackTrace();
			return null;
		}
	}
	
	static Quest deserialize(Map<String, Object> map){
		if (!map.containsKey("id")) {
			BeautyQuests.getInstance().getLogger().severe("Quest doesn't have an id.");
			return null;
		}
		NPC npc = CitizensAPI.getNPCRegistry().getById((int) map.get("starterID"));
		if (npc == null){
			BeautyQuests.getInstance().getLogger().severe("The NPC " + map.get("starterID") + " no longer exists. Quest " + map.get("id") + " cannot be loaded.");
			return null;
		}
		Quest qu = new Quest((String) map.get("name"), npc, (int) map.get("id"));
		
		qu.manager = StageManager.deserialize((Map<String, Object>) map.get("manager"), qu);
		if (qu.manager == null) {
			//qu.unloadAll();
			return null;
		}
		if (map.containsKey("multiple")) qu.repeatable = (boolean) map.get("multiple"); // TODO remove on 0.14
		if (map.containsKey("repeatable")) qu.repeatable = (boolean) map.get("repeatable");
		if (map.containsKey("hid")) qu.hid = (boolean) map.get("hid");
		if (map.containsKey("scoreboard")) qu.scoreboard = (boolean) map.get("scoreboard");
		if (map.containsKey("endMessage")) qu.endMessage = (String) map.get("endMessage");
		if (map.containsKey("startDialog")) qu.dialog = Dialog.deserialize((Map<String, Object>) map.get("startDialog"));
		if (map.containsKey("finished")) Utils.deserializeAccountsList(qu.finished, (List<String>) map.get("finished"));
		if (map.containsKey("hologramText")) qu.hologramText = (String) map.get("hologramText");
		if (map.containsKey("bypassLimit")) qu.bypassLimit = (boolean) map.get("bypassLimit");
		if (map.containsKey("timer")) qu.timer = (int) map.get("timer");
		if (map.get("inTimer") != null){
			Map<String, String> list = (Map<String, String>) map.get("inTimer");
			for (Entry<String, String> en : list.entrySet()){
				try {
					PlayerAccount acc = PlayersManager.getByIndex(en.getKey());
					if (acc != null) qu.inTimer.put(acc, Utils.getDateFormat().parse(en.getValue()).getTime());
				} catch (ParseException e) {
					BeautyQuests.loadingFailure = true;
					continue;
				}
			}
		}
		
		if (map.containsKey("requirements")){
			List<Map<String, Object>> rlist = (List<Map<String, Object>>) map.get("requirements");
			for (Map<String, Object> rmap : rlist){
				try {
					qu.requirements.add(AbstractRequirement.deserialize(rmap, qu));
				} catch (Throwable e) {
					BeautyQuests.getInstance().getLogger().severe("Error while deserializing a requirement (class " + rmap.get("class") + ").");
					BeautyQuests.loadingFailure = true;
					e.printStackTrace();
					continue;
				}
			}
		}
		if (map.containsKey("rewardsList")){
			List<Map<String, Object>> rlist = (List<Map<String, Object>>) map.get("rewardsList");
			for (Map<String, Object> rmap : rlist){
				try {
					AbstractReward rew = AbstractReward.deserialize(rmap, qu);
					qu.rewards.add(rew);
					if (rew.isAsync()) qu.asyncEnd = true;
				} catch (Throwable e) {
					BeautyQuests.getInstance().getLogger().severe("Error while deserializing a reward (class " + rmap.get("class") + ").");
					BeautyQuests.loadingFailure = true;
					e.printStackTrace();
					continue;
				}
			}
		}
		if (map.containsKey("startRewardsList")){
			List<Map<String, Object>> rlist = (List<Map<String, Object>>) map.get("startRewardsList");
			for (Map<String, Object> rmap : rlist){
				try {
					AbstractReward rew = AbstractReward.deserialize(rmap, qu);
					qu.startRewards.add(rew);
					if (rew.isAsync() && qu.asyncStart != null) qu.asyncStart = new ArrayList<>();
				} catch (Throwable e) {
					BeautyQuests.getInstance().getLogger().severe("Error while deserializing a reward (class " + rmap.get("class") + ").");
					BeautyQuests.loadingFailure = true;
					e.printStackTrace();
					continue;
				}
			}
		}
		
		return qu;
	}
	
}
