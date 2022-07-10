package fr.skytasul.quests.structure;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.PlayerQuestResetEvent;
import fr.skytasul.quests.api.events.QuestFinishEvent;
import fr.skytasul.quests.api.events.QuestLaunchEvent;
import fr.skytasul.quests.api.events.QuestPreLaunchEvent;
import fr.skytasul.quests.api.events.QuestRemoveEvent;
import fr.skytasul.quests.api.options.OptionSet;
import fr.skytasul.quests.api.options.QuestOption;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.requirements.AbstractRequirement;
import fr.skytasul.quests.api.requirements.Actionnable;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.options.*;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.rewards.MessageReward;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;

public class Quest implements Comparable<Quest>, OptionSet {
	
	private final int id;
	private final File file;
	private BranchesManager manager;
	
	private List<QuestOption<?>> options = new ArrayList<>();
	
	private boolean removed = false;
	public boolean asyncEnd = false;
	public List<Player> asyncStart = null;
	
	public Quest(int id) {
		this(id, new File(BeautyQuests.getInstance().getQuestsManager().getSaveFolder(), id + ".yml"));
	}
	
	public Quest(int id, File file) {
		this.id = id;
		this.file = file;
		this.manager = new BranchesManager(this);
	}
	
	public void load() {
		QuestsAPI.propagateQuestsHandlers(handler -> handler.questLoaded(this));
	}
	
	@Override
	public Iterator<QuestOption> iterator() {
		return (Iterator) options.iterator();
	}
	
	public <D> D getOptionValueOrDef(Class<? extends QuestOption<D>> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return (D) option.getValue();
		}
		return (D) QuestOptionCreator.creators.get(clazz).defaultValue;
	}
	
	@Override
	public <T extends QuestOption<?>> T getOption(Class<T> clazz) {
		for (QuestOption<?> option : options) {
			if (clazz.isInstance(option)) return (T) option;
		}
		throw new NullPointerException("Quest " + id + " do not have option " + clazz.getName());
	}
	
	@Override
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
	
	public ItemStack getQuestItem() {
		return getOptionValueOrDef(OptionQuestItem.class);
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
	
	public String getTimeLeft(PlayerAccount acc) {
		return Utils.millisToHumanString(acc.getQuestDatas(this).getTimer() - System.currentTimeMillis());
	}

	public boolean hasStarted(PlayerAccount acc){
		if (!acc.hasQuestDatas(this)) return false;
		if (acc.getQuestDatas(this).hasStarted()) return true;
		if (acc.isCurrent() && asyncStart != null && asyncStart.contains(acc.getPlayer())) return true;
		return false;
	}

	public boolean hasFinished(PlayerAccount acc){
		return acc.hasQuestDatas(this) && acc.getQuestDatas(this).isFinished();
	}
	
	public void cancelPlayer(PlayerAccount acc){
		manager.remove(acc);
		QuestsAPI.propagateQuestsHandlers(handler -> handler.questReset(acc, this));
		Bukkit.getPluginManager().callEvent(new PlayerQuestResetEvent(acc, this));
		
		if (acc.isCurrent())
			Utils.giveRewards(acc.getPlayer(), getOptionValueOrDef(OptionCancelRewards.class));
	}
	
	public boolean resetPlayer(PlayerAccount acc){
		if (acc == null) return false;
		boolean c = false;
		if (acc.hasQuestDatas(this)) {
			cancelPlayer(acc);
			acc.removeQuestDatas(this);
			c = true;
		}
		if (acc.isCurrent() && hasOption(OptionStartDialog.class) && getOption(OptionStartDialog.class).getDialogRunner().removePlayer(acc.getPlayer())) c = true;
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
		if (QuestsConfiguration.getMaxLaunchedQuests() != 0 && Boolean.FALSE.equals(getOptionValueOrDef(OptionBypassLimit.class))) {
			if (QuestsAPI.getQuests().getStartedSize(acc) >= QuestsConfiguration.getMaxLaunchedQuests()) {
				if (sendMessage) Lang.QUESTS_MAX_LAUNCHED.send(p, QuestsConfiguration.getMaxLaunchedQuests());
				return false;
			}
		}
		sendMessage = sendMessage && (!hasOption(OptionStarterNPC.class) || (QuestsConfiguration.isRequirementReasonSentOnMultipleQuests() || getOption(OptionStarterNPC.class).getValue().getQuests().size() == 1));
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
			if (time > System.currentTimeMillis()) {
				if (sendMessage && acc.isCurrent()) Lang.QUEST_WAIT.send(acc.getPlayer(), getTimeLeft(acc));
				return false;
			}else if (time != 0) acc.getQuestDatas(this).setTimer(0);
		}
		return true;
	}
	
	public boolean isInDialog(Player p) {
		return hasOption(OptionStartDialog.class) && getOption(OptionStartDialog.class).getDialogRunner().isPlayerInDialog(p);
	}
	
	public void clickNPC(Player p){
		if (hasOption(OptionStartDialog.class)) {
			getOption(OptionStartDialog.class).getDialogRunner().onClick(p);
		}else attemptStart(p, null);
	}
	
	public void leave(Player p) {
		if (hasOption(OptionStartDialog.class)) {
			getOption(OptionStartDialog.class).getDialogRunner().removePlayer(p);
		}
	}
	
	public String getDescriptionLine(PlayerAccount acc, Source source) {
		if (!acc.hasQuestDatas(this)) throw new IllegalArgumentException("Account does not have quest datas for quest " + id);
		if (asyncStart != null && acc.isCurrent() && asyncStart.contains(acc.getPlayer())) return "§7x";
		PlayerQuestDatas datas = acc.getQuestDatas(this);
		if (datas.isInQuestEnd()) return Lang.SCOREBOARD_ASYNC_END.toString();
		QuestBranch branch = manager.getBranch(datas.getBranch());
		if (branch == null) throw new IllegalStateException("Account is in branch " + datas.getBranch() + " in quest " + id + ", which does not actually exist");
		return branch.getDescriptionLine(acc, source);
	}

	public void attemptStart(Player p, Runnable atStart) {
		String confirm;
		if (QuestsConfiguration.questConfirmGUI() && !"none".equals(confirm = getOptionValueOrDef(OptionConfirmMessage.class))) {
			new ConfirmGUI(() -> {
				start(p);
				if (atStart != null) atStart.run();
			}, () -> Inventories.closeAndExit(p), Lang.INDICATION_START.format(getName()), confirm).create(p);
		}else {
			start(p);
			if (atStart != null) atStart.run();
		}
	}
	
	public void start(Player p){
		start(p, false);
	}
	
	public void start(Player p, boolean silently) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (hasStarted(acc)){
			if (!silently) Lang.ALREADY_STARTED.send(p);
			return;
		}
		QuestPreLaunchEvent event = new QuestPreLaunchEvent(p, this);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;
		AdminMode.broadcast(p.getName() + " started the quest " + id);
		acc.getQuestDatas(this).setTimer(0);
		if (!silently) {
			String startMsg = getOptionValueOrDef(OptionStartMessage.class);
			if (!"none".equals(startMsg)) Utils.IsendMessage(p, startMsg, true, getName());
		}
		
		Runnable run = () -> {
			List<String> msg = Utils.giveRewards(p, getOptionValueOrDef(OptionStartRewards.class));
			getOptionValueOrDef(OptionRequirements.class).stream().filter(Actionnable.class::isInstance).map(Actionnable.class::cast).forEach(x -> x.trigger(p));
			if (!silently && !msg.isEmpty()) Utils.sendMessage(p, Lang.FINISHED_OBTAIN.format(Utils.itemsToFormattedString(msg.toArray(new String[0]))));
			if (asyncStart != null) asyncStart.remove(p);
			
			Utils.runOrSync(() -> {
				manager.startPlayer(acc);
				QuestsAPI.propagateQuestsHandlers(handler -> handler.questStart(acc, p, this));
				Bukkit.getPluginManager().callEvent(new QuestLaunchEvent(p, Quest.this));
			});
		};
		if (asyncStart != null){
			asyncStart.add(p);
			Utils.runAsync(run);
		}else run.run();
	}
	
	public void finish(Player p){
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		AdminMode.broadcast(p.getName() + " is completing the quest " + id);
		PlayerQuestDatas questDatas = acc.getQuestDatas(Quest.this);
		
		Runnable run = () -> {
			try {
				List<String> msg = Utils.giveRewards(p, getOptionValueOrDef(OptionEndRewards.class));
				String obtained = Utils.itemsToFormattedString(msg.toArray(new String[0]));
				if (hasOption(OptionEndMessage.class)) {
					String endMsg = getOption(OptionEndMessage.class).getValue();
					if (!"none".equals(endMsg)) Utils.IsendMessage(p, endMsg, true, obtained);
				}else Utils.sendMessage(p, Lang.FINISHED_BASE.format(getName()) + (msg.isEmpty() ? "" : " " + Lang.FINISHED_OBTAIN.format(obtained)));
			}catch (Exception ex) {
				Lang.ERROR_OCCURED.send(p, "reward message");
				BeautyQuests.logger.severe("An error occurred while giving quest end rewards.", ex);
			}
			
			Utils.runOrSync(() -> {
				manager.remove(acc);
				questDatas.setBranch(-1);
				questDatas.incrementFinished();
				questDatas.setStartingTime(0);
				if (hasOption(OptionQuestPool.class)) getOptionValueOrDef(OptionQuestPool.class).questCompleted(acc, Quest.this);
				if (isRepeatable()) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MINUTE, Math.max(0, getOptionValueOrDef(OptionTimer.class)));
					questDatas.setTimer(cal.getTimeInMillis());
				}
				Utils.spawnFirework(p.getLocation(), getOptionValueOrDef(OptionFirework.class));
				Utils.playPluginSound(p, getOptionValueOrDef(OptionEndSound.class), 1);
				
				QuestsAPI.propagateQuestsHandlers(handler -> handler.questFinish(acc, p, this));
				Bukkit.getPluginManager().callEvent(new QuestFinishEvent(p, Quest.this));
			});
		};
		
		if (asyncEnd) {
			questDatas.setInQuestEnd();
			new Thread(() -> {
				DebugUtils.logMessage("Using " + Thread.currentThread().getName() + " as the thread for async rewards.");
				run.run();
			}, "BQ async end " + p.getName()).start();
		}else run.run();
	}

	public void remove(boolean msg, boolean removeDatas) {
		QuestsAPI.getQuests().removeQuest(this);
		unload();
		if (removeDatas) {
			PlayersManager.manager.removeQuestDatas(this);
			if (file.exists()) file.delete();
		}
		removed = true;
		Bukkit.getPluginManager().callEvent(new QuestRemoveEvent(this));
		if (removeDatas) QuestsAPI.propagateQuestsHandlers(handler -> handler.questRemove(this));
		if (msg) BeautyQuests.getInstance().getLogger().info("The quest \"" + getName() + "\" has been removed");
	}
	
	public void unload(){
		QuestsAPI.propagateQuestsHandlers(handler -> handler.questUnload(this));
		manager.remove();
		options.forEach(QuestOption::detach);
	}

	@Override
	public int compareTo(Quest o) {
		return Integer.compare(id, o.id);
	}
	
	@Override
	public String toString(){
		return "Quest{id=" + id + ", npcID=" + ", branches=" + manager.toString() + ", name=" + getName() + "}";
	}

	public boolean saveToFile() throws Exception {
		if (!file.exists()) file.createNewFile();
		YamlConfiguration fc = new YamlConfiguration();
		
		BeautyQuests.savingFailure = false;
		save(fc);
		if (BeautyQuests.savingFailure) {
			BeautyQuests.logger.warning("An error occurred while saving quest " + id);
			return false;
		}
		String questData = fc.saveToString();
		String oldQuestDatas = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
		if (questData.equals(oldQuestDatas)) {
			DebugUtils.logMessage("Quest " + id + " was up-to-date.");
			return false;
		}else {
			DebugUtils.logMessage("Saving quest " + id + " into " + file.getPath());
			Files.write(file.toPath(), questData.getBytes(StandardCharsets.UTF_8));
			return true;
		}
	}
	
	private void save(ConfigurationSection section) throws Exception{
		for (QuestOption<?> option : options) {
			try {
				if (option.hasCustomValue()) section.set(option.getOptionCreator().id, option.save());
			}catch (Exception ex) {
				BeautyQuests.logger.warning("An exception occured when saving an option for quest " + id, ex);
			}
		}
		
		manager.save(section.createSection("manager"));
		section.set("id", id);
	}
	

	public static Quest loadFromFile(File file){
		try {
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			return deserialize(file, config);
		}catch (Exception e) {
			BeautyQuests.logger.warning("Error when loading quests from data file.", e);
			return null;
		}
	}
	
	private static Quest deserialize(File file, ConfigurationSection map) {
		if (!map.contains("id")) {
			BeautyQuests.getInstance().getLogger().severe("Quest doesn't have an id.");
			return null;
		}
		
		Quest qu = new Quest(map.getInt("id"), file);
		
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
						BeautyQuests.logger.warning("An exception occured when loading the option " + key + " for quest " + qu.id, ex);
						BeautyQuests.loadingFailure = true;
					}
					break;
				}
			}
		}
		String endMessage = map.getString("endMessage");
		if (endMessage != null) {
			OptionEndRewards rewards;
			if (qu.hasOption(OptionEndRewards.class)) {
				rewards = qu.getOption(OptionEndRewards.class);
			}else {
				rewards = (OptionEndRewards) QuestOptionCreator.creators.get(OptionEndRewards.class).optionSupplier.get();
				rewards.getValue().add(new MessageReward(endMessage));
				qu.addOption(rewards);
			}
		}

		return qu;
	}
	
}
