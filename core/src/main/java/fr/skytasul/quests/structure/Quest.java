package fr.skytasul.quests.structure;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.api.rewards.AbstractReward;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Dynmap;
import fr.skytasul.quests.utils.types.Dialog;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class Quest{
	
	private final int id;
	private final File file;
	private BranchesManager manager;
	
	private String name;
	private String endMessage;
	private String hologramText;
	private String customConfirmMessage;
	private NPC npcStarter;
	private Dialog dialog;
	private List<AbstractRequirement> requirements = new ArrayList<>();
	private List<AbstractReward> rewards = new ArrayList<>();
	private List<AbstractReward> startRewards = new ArrayList<>();
	private boolean repeatable = false;
	private boolean cancellable = true;
	private boolean scoreboard = true;
	private boolean hid = false;
	private boolean bypassLimit = false;
	private int timer = -1;
	private ItemStack hologramLaunch;
	private ItemStack hologramLaunchNo;
	
	private final List<PlayerAccount> finished = new ArrayList<>();
	private final Map<PlayerAccount, Long> inTimer = new HashMap<>();
	
	private boolean removed = false;
	private boolean asyncEnd = false;
	private List<Player> asyncStart = null;
	
	List<Player> launcheable = new ArrayList<>();
	private List<Player> particles = new ArrayList<>();
	
	public Quest(String name, NPC npc, int id){
		this.name = name;
		this.manager = new BranchesManager(this);
		this.npcStarter = npc;
		this.id = id;
		if (id >= BeautyQuests.lastID) BeautyQuests.lastID = id;
		this.file = new File(BeautyQuests.saveFolder, id + ".yml");
	}
	
	public void create() {
		if (Dependencies.dyn) Dynmap.addMarker(this);
	}
	
	void updateLauncheable(LivingEntity en) {
		if (QuestsConfiguration.showStartParticles()) {
			if (launcheable.isEmpty()) return;
			particles.clear();
			particles.addAll(launcheable);
			QuestsConfiguration.getParticleStart().send(en, particles);
		}
	}
	
	
	public String getName(){
		return name;
	}
	
	public BranchesManager getBranchesManager(){
		return manager;
	}
	
	public String getCustomHologramText(){
		return hologramText;
	}
	
	public void setHologramText(String hologramText) {
		this.hologramText = hologramText;
	}
	
	public String getCustomConfirmMessage(){
		return customConfirmMessage;
	}
	
	public void setCustomConfirmMessage(String message) {
		this.customConfirmMessage = message;
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
			if (rew.isAsync()) asyncEnd = true;
			rew.setQuest(this);
		}
	}
	
	public List<AbstractReward> getStartRewards() {
		return new ArrayList<>(startRewards);
	}

	public void setStartRewards(List<AbstractReward> rewards) {
		this.startRewards = rewards;
		this.asyncStart = null;
		for(AbstractReward rew : startRewards){
			if (rew.isAsync() && asyncStart == null) asyncStart = new ArrayList<>();
			rew.setQuest(this);
		}
	}

	public List<AbstractRequirement> getRequirements(){
		return new ArrayList<>(requirements);
	}

	public void setRequirements(List<AbstractRequirement> requirements) {
		this.requirements = requirements;
		for(AbstractRequirement req : requirements){
			req.setQuest(this);
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
	
	public void setCancellable(boolean cancellable){
		this.cancellable = cancellable;
	}
	
	public boolean isCancellable(){
		return cancellable;
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
	
	public void setEndMessage(String msg){
		this.endMessage = msg;
	}
	
	public String getEndMessage(){
		return endMessage;
	}
	
	public ItemStack getCustomHologramLaunch(){
		return hologramLaunch == null ? QuestsConfiguration.getHoloLaunchItem() : hologramLaunch;
	}
	
	public void setHologramLaunch(ItemStack hologram){
		this.hologramLaunch = hologram;
	}
	
	public ItemStack getCustomHologramLaunchNo(){
		return hologramLaunchNo == null ? QuestsConfiguration.getHoloLaunchNoItem() : hologramLaunchNo;
	}
	
	public void setHologramLaunchNo(ItemStack hologram){
		this.hologramLaunchNo = hologram;
	}
	
	public int getID(){
		return id;
	}
	
	public File getFile(){
		return file;
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
	
	public void cancelPlayer(PlayerAccount acc){
		Bukkit.getPluginManager().callEvent(new PlayerQuestResetEvent(acc, this));
		manager.remove(acc);
	}
	
	public boolean resetPlayer(PlayerAccount acc){
		if (acc == null) return false;
		boolean c = false;
		if (manager.contains(acc)){
			Bukkit.getPluginManager().callEvent(new PlayerQuestResetEvent(acc, this));
			manager.remove(acc);
			c = true;
		}
		if (finished.remove(acc)) c = true;
		if (inTimer.remove(acc) != null) c = true;
		if (acc.isCurrent() && dialog.remove(acc.getPlayer())) c = true;
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
				if (sendMessage) Lang.QUEST_WAIT.send(p, getTimeLeft(acc));
				return false;
			}
			inTimer.remove(acc);
		}
		return true;
	}
	
	public boolean isInDialog(Player p) {
		return dialog.isInDialog(p);
	}
	
	public void clickNPC(Player p){
		if (dialog != null) {
			dialog.send(p, () -> start(p));
		}else start(p);
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
				requirements.stream().filter(Actionnable.class::isInstance).map(Actionnable.class::cast).forEach(x -> x.trigger(p));
				if (!msg.isEmpty()) Utils.sendMessage(p, Lang.FINISHED_OBTAIN.format(Utils.itemsToFormattedString(msg.toArray(new String[0]))));
				manager.startPlayer(acc);
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
				
				if (endMessage != null) Utils.sendOffMessage(p, endMessage);
				manager.remove(acc);
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
		if (Dependencies.dyn) Dynmap.removeMarker(this);
		for (AbstractReward rew : rewards){
			rew.unload();
		}
		for (AbstractRequirement req : requirements){
			req.unload();
		}
	}
	
	public String toString(){
		return "Quest{id=" + id + ",npcID=" + npcStarter.getId() + ",branches=" + manager.toString() + ",name=" + name + "}";
	}
	

	public void saveToFile(boolean unload) throws Exception{
		if (!file.exists()) file.createNewFile();
		YamlConfiguration fc = new YamlConfiguration();
		
		save(fc);
		if (BeautyQuests.savingFailure) BeautyQuests.getInstance().createQuestBackup(file, id + "", "Error when saving quest.");
		fc.save(file);
		
		if (unload) unloadAll();
	}
	
	private void save(ConfigurationSection section) throws Exception{
		section.set("name", name);
		section.set("id", id);
		section.set("manager", manager.serialize());
		section.set("starterID", npcStarter.getId());
		section.set("scoreboard", scoreboard);
		section.set("finished", Utils.serializeAccountsList(finished));
		if (repeatable) section.set("repeatable", repeatable);
		if (!cancellable) section.set("cancellable", cancellable);
		if (hologramText != null) section.set("hologramText", hologramText);
		if (customConfirmMessage != null) section.set("confirmMessage", customConfirmMessage);
		if (hid) section.set("hid", true);
		if (endMessage != null) section.set("endMessage", endMessage);
		if (dialog != null) section.set("startDialog", dialog.serialize());
		if (bypassLimit) section.set("bypassLimit", bypassLimit);
		if (timer > -1) section.set("timer", timer);
		if (hologramLaunch != null) section.set("hologramLaunch", hologramLaunch.serialize());
		if (hologramLaunchNo != null) section.set("hologramLaunchNo", hologramLaunchNo.serialize());
		
		if (!inTimer.isEmpty()){
			Map<String, String> tmap = new HashMap<>();
			for (Entry<PlayerAccount, Long> en : inTimer.entrySet()){
				tmap.put(en.getKey().getIndex(), Utils.getDateFormat().format(new Date(en.getValue())));
			}
			section.set("inTimer", tmap);
		}
		
		section.set("requirements", Utils.serializeList(requirements, AbstractRequirement::serialize));
		section.set("rewardsList", Utils.serializeList(rewards, AbstractReward::serialize));
		section.set("startRewardsList", Utils.serializeList(startRewards, AbstractReward::serialize));
	}
	

	public static Quest loadFromFile(File file){
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			return deserialize(config.isList("quest") ? (Map<String, Object>) config.getMapList("quest").get(0) : Utils.mapFromConfigurationSection(config));
		}catch (Exception e) {
			BeautyQuests.logger.warning("Error when loading quests from data file.");
			e.printStackTrace();
			return null;
		}
	}
	
	private static Quest deserialize(Map<String, Object> map){
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
		
		qu.manager = BranchesManager.deserialize((Map<String, Object>) map.get("manager"), qu);
		if (qu.manager == null) {
			//qu.unloadAll();
			return null;
		}
		if (map.containsKey("repeatable")) qu.repeatable = (boolean) map.get("repeatable");
		if (map.containsKey("cancellable")) qu.cancellable = (boolean) map.get("cancellable");
		if (map.containsKey("hid")) qu.hid = (boolean) map.get("hid");
		if (map.containsKey("scoreboard")) qu.scoreboard = (boolean) map.get("scoreboard");
		if (map.containsKey("endMessage")) qu.endMessage = (String) map.get("endMessage");
		if (map.containsKey("startDialog")) qu.dialog = Dialog.deserialize((Map<String, Object>) map.get("startDialog"));
		if (map.containsKey("finished")) Utils.deserializeAccountsList(qu.finished, (List<String>) map.get("finished"));
		if (map.containsKey("hologramText")) qu.hologramText = (String) map.get("hologramText");
		if (map.containsKey("confirmMessage")) qu.customConfirmMessage = (String) map.get("confirmMessage");
		if (map.containsKey("bypassLimit")) qu.bypassLimit = (boolean) map.get("bypassLimit");
		if (map.containsKey("hologramLaunch")) qu.hologramLaunch = ItemStack.deserialize((Map<String, Object>) map.get("hologramLaunch"));
		if (map.containsKey("hologramLaunchNo")) qu.hologramLaunchNo = ItemStack.deserialize((Map<String, Object>) map.get("hologramLaunchNo"));
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
