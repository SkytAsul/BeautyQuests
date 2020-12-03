package fr.skytasul.quests.structure;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestPreLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.options.OptionBypassLimit;
import fr.skytasul.quests.options.OptionCancellable;
import fr.skytasul.quests.options.OptionConfirmMessage;
import fr.skytasul.quests.options.OptionDescription;
import fr.skytasul.quests.options.OptionEndMessage;
import fr.skytasul.quests.options.OptionEndRewards;
import fr.skytasul.quests.options.OptionHide;
import fr.skytasul.quests.options.OptionHideNoRequirements;
import fr.skytasul.quests.options.OptionName;
import fr.skytasul.quests.options.OptionQuestMaterial;
import fr.skytasul.quests.options.OptionQuestPool;
import fr.skytasul.quests.options.OptionRepeatable;
import fr.skytasul.quests.options.OptionRequirements;
import fr.skytasul.quests.options.OptionScoreboardEnabled;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.options.OptionStartRewards;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.options.OptionTimer;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.Dynmap;

public class Quest implements Comparable<Quest> {
	
	private final int id;
	private final File file;
	private BranchesManager manager;
	
	private List<QuestOption<?>> options = new ArrayList<>();
	
	private boolean removed = false;
	public boolean asyncEnd = false;
	public List<Player> asyncStart = null;
	
	List<Player> launcheable = new ArrayList<>();
	private List<Player> particles = new ArrayList<>();
	
	public Quest(int id) {
		this.manager = new BranchesManager(this);
		this.id = id;
		if (id >= BeautyQuests.lastID) BeautyQuests.lastID = id;
		this.file = new File(BeautyQuests.saveFolder, id + ".yml");
	}
	
	public void create() {
		if (DependenciesManager.dyn) Dynmap.addMarker(this);
	}
	
	void updateLauncheable(LivingEntity en) {
		if (QuestsConfiguration.showStartParticles()) {
			if (launcheable.isEmpty()) return;
			particles.clear();
			particles.addAll(launcheable);
			QuestsConfiguration.getParticleStart().send(en, particles);
		}
	}
	
	public <D> D getOptionValueOrDef(Class<? extends QuestOption<D>> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return (D) option.getValue();
		}
		return (D) QuestOptionCreator.creators.get(clazz).defaultValue;
	}
	
	public <T extends QuestOption<?>> T getOption(Class<T> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return (T) option;
		}
		throw new NullPointerException("Quest " + id + " do not have option " + clazz.getName());
	}
	
	public boolean hasOption(Class<? extends QuestOption<?>> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return true;
		}
		return false;
	}
	
	public void addOption(QuestOption<?> option) {
		if (!option.hasCustomValue()) return;
		options.add(option);
		option.attach(this);
		option.setValueUpdaterListener(() -> {
			if (!option.hasCustomValue()) {
				option.detach();
				options.remove(option);
			}
		});
	}
	
	public void removeOption(Class<? extends QuestOption<?>> clazz) {
		for (Iterator<QuestOption<?>> iterator = options.iterator(); iterator.hasNext();) {
			QuestOption<?> option = iterator.next();
			if (clazz.isInstance(option)) {
				option.detach();
				iterator.remove();
				break;
			}
		}
	}
	
	public boolean isRemoved(){
		return removed;
	}
	
	public int getID(){
		return id;
	}
	
	public File getFile(){
		return file;
	}
	
	public String getName(){
		return getOptionValueOrDef(OptionName.class);
	}
	
	public String getDescription() {
		return getOptionValueOrDef(OptionDescription.class);
	}
	
	public XMaterial getQuestMaterial() {
		return getOptionValueOrDef(OptionQuestMaterial.class);
	}
	
	public boolean isScoreboardEnabled() {
		return getOptionValueOrDef(OptionScoreboardEnabled.class);
	}
	
	public boolean isCancellable() {
		return getOptionValueOrDef(OptionCancellable.class);
	}
	
	public boolean isRepeatable() {
		return getOptionValueOrDef(OptionRepeatable.class);
	}
	
	public boolean isHidden() {
		return getOptionValueOrDef(OptionHide.class);
	}
	
	public boolean isHiddenWhenRequirementsNotMet() {
		return getOptionValueOrDef(OptionHideNoRequirements.class);
	}
	
	public boolean canBypassLimit() {
		return getOptionValueOrDef(OptionBypassLimit.class);
	}
	
	public BranchesManager getBranchesManager(){
		return manager;
	}
	
	public int getTimeLeft(PlayerAccount acc){
		return Math.max((int) Math.ceil((acc.getQuestDatas(this).getTimer() - System.currentTimeMillis()) / 60000D), 0);
	}

	public boolean hasStarted(PlayerAccount acc){
		if (!acc.hasQuestDatas(this)) return false;
		if (acc.getQuestDatas(this).getBranch() != -1) return true;
		if (acc.isCurrent() && asyncStart != null && asyncStart.contains(acc.getPlayer())) return true;
		return false;
	}

	public boolean hasFinished(PlayerAccount acc){
		return acc.hasQuestDatas(this) ? acc.getQuestDatas(this).isFinished() : false;
	}
	
	public void cancelPlayer(PlayerAccount acc){
		manager.remove(acc);
		acc.removeQuestDatas(this);
		Bukkit.getPluginManager().callEvent(new PlayerQuestResetEvent(acc, this));
	}
	
	public boolean resetPlayer(PlayerAccount acc){
		if (acc == null) return false;
		boolean c = false;
		if (acc.hasQuestDatas(this)) {
			cancelPlayer(acc);
			c = true;
		}
		if (acc.isCurrent() && hasOption(OptionStartDialog.class) && getOption(OptionStartDialog.class).getValue().remove(acc.getPlayer())) c = true;
		return c;
	}
	
	public boolean isLauncheable(Player p, PlayerAccount acc, boolean sendMessage) {
		if (hasStarted(acc)){
			if (sendMessage) Lang.ALREADY_STARTED.send(p);
			return false;
		}
		if (!getOptionValueOrDef(OptionRepeatable.class) && hasFinished(acc)) return false;
		if (!testTimer(acc, sendMessage)) return false;
		if (!testRequirements(p, acc, sendMessage)) return false;
		return true;
	}
	
	public boolean testRequirements(Player p, PlayerAccount acc, boolean sendMessage){
		if (!p.hasPermission("beautyquests.start")) return false;
		if (QuestsConfiguration.getMaxLaunchedQuests() != 0 && !getOptionValueOrDef(OptionBypassLimit.class)) {
			if (QuestsAPI.getStartedSize(acc) >= QuestsConfiguration.getMaxLaunchedQuests()) return false;
		}
		sendMessage = sendMessage && (!hasOption(OptionStarterNPC.class) || (QuestsConfiguration.isRequirementReasonSentOnMultipleQuests() || QuestsAPI.getQuestsAssigneds(getOption(OptionStarterNPC.class).getValue()).size() == 1));
		for (AbstractRequirement ar : getOptionValueOrDef(OptionRequirements.class)) {
			if (!ar.test(p)) {
				if (sendMessage) ar.sendReason(p);
				return false;
			}
		}
		return true;
	}
	
	public boolean testTimer(PlayerAccount acc, boolean sendMessage) {
		if (isRepeatable() && acc.hasQuestDatas(this)) {
			long time = acc.getQuestDatas(this).getTimer();
			if (time > System.currentTimeMillis()){
				if (sendMessage && acc.isCurrent()) Lang.QUEST_WAIT.send(acc.getPlayer(), getTimeLeft(acc));
				return false;
			}
			acc.getQuestDatas(this).setTimer(0);
		}
		return true;
	}
	
	public boolean isInDialog(Player p) {
		return hasOption(OptionStartDialog.class) && getOption(OptionStartDialog.class).getValue().isInDialog(p);
	}
	
	public void clickNPC(Player p){
		if (hasOption(OptionStartDialog.class)) {
			getOption(OptionStartDialog.class).getValue().send(p, getOptionValueOrDef(OptionStarterNPC.class), () -> attemptStart(p));
		}else attemptStart(p);
	}

	public void attemptStart(Player p) {
		if (QuestsConfiguration.questConfirmGUI()) {
			new ConfirmGUI(() -> start(p), () -> Inventories.closeAndExit(p), Lang.INDICATION_START.format(getName()), getOptionValueOrDef(OptionConfirmMessage.class)).create(p);
		}else start(p);
	}
	
	public void start(Player p){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			Lang.ALREADY_STARTED.send(p);
			return;
		}
		QuestPreLaunchEvent event = new QuestPreLaunchEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		AdminMode.broadcast(p.getName() + " started the quest " + id);
		launcheable.remove(p);
		acc.getQuestDatas(this).setTimer(0);
		Lang.STARTED_QUEST.send(p, getName());
		
		BukkitRunnable run = new BukkitRunnable() {
			public void run(){
				List<String> msg = Utils.giveRewards(p, getOptionValueOrDef(OptionStartRewards.class));
				getOptionValueOrDef(OptionRequirements.class).stream().filter(Actionnable.class::isInstance).map(Actionnable.class::cast).forEach(x -> x.trigger(p));
				if (!msg.isEmpty()) Utils.sendMessage(p, Lang.FINISHED_OBTAIN.format(Utils.itemsToFormattedString(msg.toArray(new String[0]))));
				manager.startPlayer(acc);

				Utils.runOrSync(() -> Bukkit.getPluginManager().callEvent(new QuestLaunchEvent(p, Quest.this)));
			}
		};
		if (asyncStart != null){
			asyncStart.add(p);
			run.runTaskAsynchronously(BeautyQuests.getInstance());
		}else run.run();
	}
	
	public void finish(Player p){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		AdminMode.broadcast(p.getName() + " completed the quest " + id);
		
		BukkitRunnable run = new BukkitRunnable() {
			public void run(){
				List<String> msg = Utils.giveRewards(p, getOptionValueOrDef(OptionEndRewards.class));
				Utils.sendMessage(p, Lang.FINISHED_BASE.format(getName()) + (msg.isEmpty() ? "" : " " + Lang.FINISHED_OBTAIN.format(Utils.itemsToFormattedString(msg.toArray(new String[0])))));
				
				String endMessage = getOptionValueOrDef(OptionEndMessage.class);
				if (endMessage != null) Utils.sendOffMessage(p, endMessage);
				manager.remove(acc);
				PlayerQuestDatas questDatas = acc.getQuestDatas(Quest.this);
				questDatas.setFinished(true);
				if (hasOption(OptionQuestPool.class)) getOptionValueOrDef(OptionQuestPool.class).questCompleted(acc, Quest.this);
				if (isRepeatable()) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, getOptionValueOrDef(OptionTimer.class));
					questDatas.setTimer(cal.getTimeInMillis());
				}
				Utils.spawnFirework(p.getLocation());
				Utils.playPluginSound(p, QuestsConfiguration.getFinishSound(), 1);
				
				QuestFinishEvent event = new QuestFinishEvent(p, Quest.this);
				Utils.runOrSync(() -> Bukkit.getPluginManager().callEvent(event));
			}
		};
		if (asyncEnd){
			run.runTaskAsynchronously(BeautyQuests.getInstance());
		}else run.run();
	}

	public void remove(boolean msg){
		BeautyQuests.getInstance().removeQuest(this);
		unloadAll();
		if (file.exists()) file.delete();
		removed = true;
		Bukkit.getPluginManager().callEvent(new QuestRemoveEvent(this));
		if (msg) BeautyQuests.getInstance().getLogger().info("The quest \"" + getName() + "\" has been removed");
	}
	
	public void unloadAll(){
		manager.remove();
		if (DependenciesManager.dyn) Dynmap.removeMarker(this);
		options.forEach(QuestOption::detach);
	}

	@Override
	public int compareTo(Quest o) {
		return Integer.compare(id, o.id);
	}
	
	public String toString(){
		return "Quest{id=" + id + ", npcID=" + ", branches=" + manager.toString() + ", name=" + getName() + "}";
	}

	public void saveToFile() throws Exception {
		if (!file.exists()) file.createNewFile();
		YamlConfiguration fc = new YamlConfiguration();
		
		BeautyQuests.savingFailure = false;
		save(fc);
		if (BeautyQuests.savingFailure) BeautyQuests.getInstance().createQuestBackup(file, id + "", "Error when saving quest.");
		fc.save(file);
	}
	
	private void save(ConfigurationSection section) throws Exception{
		for (QuestOption<?> option : options) {
			try {
				if (option.hasCustomValue()) section.set(option.getOptionCreator().id, option.save());
			}catch (Exception ex) {
				BeautyQuests.logger.warning("An exception occured when saving an option for quest " + id);
				ex.printStackTrace();
			}
		}
		
		section.set("id", id);
		section.set("manager", manager.serialize());
	}
	

	public static Quest loadFromFile(File file){
		try {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			return deserialize(config);
		}catch (Exception e) {
			BeautyQuests.logger.warning("Error when loading quests from data file.");
			e.printStackTrace();
			return null;
		}
	}
	
	private static Quest deserialize(ConfigurationSection map) {
		if (!map.contains("id")) {
			BeautyQuests.getInstance().getLogger().severe("Quest doesn't have an id.");
			return null;
		}
		
		Quest qu = new Quest(map.getInt("id"));
		
		qu.manager = BranchesManager.deserialize(map.getConfigurationSection("manager"), qu);
		if (qu.manager == null) return null;
		
		for (String key : map.getKeys(false)) {
			for (QuestOptionCreator<?, ?> creator : QuestOptionCreator.creators.values()) {
				if (creator.applies(key)) {
					try {
						QuestOption<?> option = creator.optionSupplier.get();
						option.load(map, key);
						qu.addOption(option);
					}catch (Exception ex) {
						BeautyQuests.logger.warning("An exception occured when loading the option " + key + " for quest " + qu.id);
						BeautyQuests.loadingFailure = true;
						ex.printStackTrace();
					}
					break;
				}
			}
		}

		return qu;
	}
	
}
