package fr.skytasul.quests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.commands.Commands;
import fr.skytasul.quests.commands.CommandsManager;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.RequirementsGUI;
import fr.skytasul.quests.gui.creation.RewardsGUI;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.SpigotUpdater;
import fr.skytasul.quests.utils.compatibility.Dependencies;
import fr.skytasul.quests.utils.compatibility.Dynmap;
import fr.skytasul.quests.utils.nms.NMS;
import net.citizensnpcs.api.npc.NPC;

public class BeautyQuests extends JavaPlugin{

	public static QuestsLogger logger;
	private static BeautyQuests instance;
	private BukkitRunnable saveTask;
	
	/* --------- YAML --------- */
	
	private String lastVersion;
	private FileConfiguration config;
	private YamlConfiguration data;
	private File dataFile;
	static File saveFolder;
	
	/* --------- Datas --------- */

	private List<Quest> quests = new ArrayList<>();
	private Map<NPC, NPCStarter> npcs = new HashMap<>();
	private ScoreboardManager scoreboards;
	public static int lastID = 0;
	
	/* ---------- Operations -------- */

	private static boolean disable = false;
	public static boolean loadingFailure = false;
	public static boolean savingFailure = false;
	
	/* ---------------------------------------------- */

	public void onLoad(){
		instance = this;
		try{
			if (!getDataFolder().exists()) getDataFolder().mkdir();
			logger = new QuestsLogger(this);
			Field f = JavaPlugin.class.getDeclaredField("logger");
			f.setAccessible(true);
			f.set(this, logger);
			System.out.println("New logger inserted: " + getLogger().getClass().getName());
		}catch (Throwable ex){
			System.out.println("Failed to insert new logger to BeautyQuests. Actual: " + getLogger().getClass().getName());
			logger = null;
			ex.printStackTrace();
		}
	}
	
	public void onEnable(){
		if (!getServer().getPluginManager().isPluginEnabled("Citizens")){
			logger.severe("Citizens plugin is not installed.");
			logger.severe("This is a fatal error. Now disabling.");
			disable = true;
			setEnabled(false);
			return;
		}
		try{
			Dependencies.initialize(getServer().getPluginManager(), logger);
		}catch (Throwable ex){
			logger.severe("Error when initializing compatibilities. Consider restarting.");
			ex.printStackTrace();
		}
		
		new BukkitRunnable() {
			public void run(){
				try {
					long lastMillis = System.currentTimeMillis();
					getLogger().info(loadAllDatas() + " quests loaded ("
							+ (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");

					launchSaveCycle();
				}catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}.runTaskLater(this, 40L);

		saveDefaultConfig();
		NMS.intializeNMS();
		registerCommands();

		saveFolder = new File(getDataFolder(), "quests");
		if (!saveFolder.exists()) saveFolder.mkdirs();
		loadDataFile();
		loadConfigParameters(true);
		
		logger.launchFlushTimer();
		try {
			new SpigotUpdater(this, 39255);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void onDisable(){
		Editor.leaveAll();
		Inventories.closeAll();
		getServer().getScheduler().cancelTasks(this);
		stopSaveCycle();
		
		try {
			if (!disable) getLogger().info(saveAllConfig(true) + " quests saved");
			if (logger != null) logger.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/* ---------- Various init ---------- */

	private void registerCommands(){
		CommandsManager questCommand = new CommandsManager((sender) -> {
			if (!(sender instanceof Player)) return;
			Player p = (Player) sender;
			if (!p.hasPermission("beautyquests.command.listPlayer")){
				Lang.INCORRECT_SYNTAX.send(p);
			}else Inventories.create(p, new PlayerListGUI(PlayersManager.getPlayerAccount(p)));
		});
		PluginCommand cmd = getCommand("beautyquests");
		cmd.setPermission("beautyquests.command");
		cmd.setExecutor(questCommand);
		cmd.setTabCompleter(questCommand);
		questCommand.registerCommandsClass(new Commands());
	}
	
	private void launchSaveCycle(){
		if (QuestsConfiguration.saveCycle > 0 && saveTask != null){
			int cycle = QuestsConfiguration.saveCycle * 60 * 20;
			saveTask = new BukkitRunnable() {
				public void run() {
					try {
						logger.info(saveAllConfig(false) + " quests saved ~ periodic save");
					}catch (Throwable e) {
						logger.severe("Error when saving !");
						e.printStackTrace();
					}
				}
			};
			logger.info("Periodic saves task started (" + cycle + " ticks). Task ID: " + saveTask.runTaskTimer(this, cycle, cycle).getTaskId());
		}
	}
	
	private void stopSaveCycle(){
		if (QuestsConfiguration.saveCycle > 0 && saveTask != null){
			saveTask.cancel();
			saveTask = null;
			logger.info("Periodic saves task stopped.");
		}
	}
	
	/* ---------- YAML ---------- */
	
	private void loadConfigParameters(boolean init){
		try{
			config = getConfig();
			/*				static initialization				*/
			if (init){
				if (loadLang() == null) return;
				StagesGUI.initialize(); // 			initializing default stage types
				RequirementsGUI.initialize(); //	initializing default requirements
				RewardsGUI.initialize(); //			initializing default rewards
			}
			
			QuestsConfiguration.initConfiguration(config);
		}catch (Exception ex){
			getLogger().severe("Error when loading.");
			ex.printStackTrace();
		}
	}
	
	private YamlConfiguration loadLang() {
		try {
			String s = "locales/" + config.getString("lang", "en_US") + ".yml";
			File file = new File(getDataFolder(), s);
			InputStream res = getResource(s);
			if (!file.exists()){
				if (res == null){ // file and local resource do not exist: using en_US
					logger.warning("Language file " + s + " does not exist. Using en_US.yml");
					s = "locales/en_US.yml";
					file = new File(getDataFolder(), s);
				}
				saveResource(s, false); // copying local resource
			}
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			if (res != null){ // if it's a local resource
				YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(res, StandardCharsets.UTF_8));
				boolean changes = false;
				for (String key : def.getKeys(true)){ // get all keys in resource
					if (!def.isConfigurationSection(key)){ // if not a block
						if (!conf.contains(key)){ // if string does not exist in the file
							conf.set(key, def.get(key)); // copy string
							DebugUtils.logMessage("String copied from source file to " + s + ". Key: " + key);
							changes = true;
						}
					}
				}
				if (changes) conf.save(file); // if there has been changes before, save the edited file
			}
			long lastMillis = System.currentTimeMillis();
			Lang.loadStrings(conf);
			getLogger().info("Loaded language file " + s + " (" + (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");
			return conf;
		} catch(Exception e) {
			e.printStackTrace();
			getLogger().severe("Couldn't create language file.");
			getLogger().severe("This is a fatal error. Now disabling.");
			disable = true;
			this.setEnabled(false);
			return null;
		}
	}
	
	private void loadDataFile(){
		dataFile = new File(getDataFolder(), "data.yml");
		if (!dataFile.exists()){
			try {
				dataFile.createNewFile();
				getLogger().info("data.yml created.");
			} catch (IOException e) {
				e.printStackTrace();
				getLogger().severe("Couldn't create data file.");
				getLogger().severe("This is a fatal error. Now disabling.");
				disable = true;
				this.setEnabled(false);
				return;
			}
		}
		DebugUtils.logMessage("Loading data file, last time edited : " + new Date(dataFile.lastModified()).toString());
		data = YamlConfiguration.loadConfiguration(dataFile);
		
		if (data.contains("version")){
			lastVersion = data.getString("version");
			if (!lastVersion.equals(getDescription().getVersion())){
				createFolderBackup("You are using a new version for the first time.");
				createDataBackup("You are using a new version for the first time.");
			}
		}else lastVersion = getDescription().getVersion();
		data.options().header("Do not edit ANYTHING here.");
		data.options().copyHeader(true);
	}
	
	private int loadAllDatas() throws Throwable{
		if (disable) return 666;
		
		File scFile = new File(getDataFolder(), "scoreboard.yml");
		if (!scFile.exists()) saveResource("scoreboard.yml", true);
		scoreboards = new ScoreboardManager(YamlConfiguration.loadConfiguration(scFile));
		if (Dependencies.dyn){
			try{
				Dynmap.intitialize();
			}catch (Throwable ex){
				getLogger().severe("An error occured while initializing dynmap integration.");
				ex.printStackTrace();
				Dynmap.unload();
			}
		}
		
		quests.clear();
		lastID = data.getInt("lastID");

		try{
			PlayersManager.load(data);
		}catch (Throwable ex){
			createDataBackup("Error when loading player datas.");
			ex.printStackTrace();
		}

		for (File file : saveFolder.listFiles()){
			if (!file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("yml") || file.getName().contains("backup")) continue;
			loadingFailure = false;
			try{
				Quest quest = Quest.loadFromFile(file);
				if (quest != null) {
					addQuest(quest);
				}else logger.severe("Quest from file " + file.getName() + " not activated");
				if (loadingFailure) createQuestBackup(file, file.getName().substring(0, file.getName().lastIndexOf(".")), "Error when loading quest.");
			}catch (Throwable ex){
				ex.printStackTrace();
				continue;
			}
		}
		QuestsConfiguration.firstQuest = QuestsAPI.getQuestFromID(QuestsConfiguration.firstQuestID);
		
		getServer().getPluginManager().registerEvents(new QuestsListener(), this);
		
		return quests.size();
	}

	public int saveAllConfig(boolean unload) throws Throwable{
		if (unload){
			if (scoreboards != null) scoreboards.unload();
		}
		
		int amount = 0;
		for (Quest qu : quests){
			savingFailure = false;
			try{
				File file = qu.file;
				if (!file.exists()) file.createNewFile();
				YamlConfiguration fc = YamlConfiguration.loadConfiguration(file);
				List<Map<String, Object>> ls = new ArrayList<>();
				ls.add(qu.serialize());
				if (savingFailure) createQuestBackup(qu.file, qu.getID() + "", "Error when saving quest.");
				fc.set("quest", ls);
				fc.save(file);
				amount++;
				
				if (unload) qu.unloadAll();
			}catch (Throwable ex){
				getLogger().warning("Error when saving quest ID " + qu.getID());
				ex.printStackTrace();
				continue;
			}
		}
		data.set("lastID", lastID);
		data.set("version", getDescription().getVersion());
		
		try{
			PlayersManager.save(data);
		}catch (Throwable ex){
			createDataBackup("Error when saving player datas.");
			ex.printStackTrace();
		}
		data.save(dataFile);
		
		if (unload){
			resetDatas();
		}
		
		return amount;
	}
	
	private void resetDatas(){
		quests.clear();
		npcs.clear();
		HandlerList.unregisterAll(this);
		if (Dependencies.dyn) Dynmap.unload();
	}
	
	/* ---------- Backups ---------- */
	
	public boolean createFolderBackup(String msg){
		getLogger().info(msg + " Creating backup...");
		try {
			File backupDir = backupDir();
			Files.copy(saveFolder.toPath(), backupDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
			getLogger().info("Quests backup created in " + backupDir.getName());
			return true;
		}catch (Exception e) {
			getLogger().severe("An error occured while creating the backup.");
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean createDataBackup(String msg){
		getLogger().info(msg + " Creating backup...");
		try{
			getLogger().info("Datas backup created in " + Files.copy(dataFile.toPath(), new File(backupDir(), "data.yml").toPath()).getParent().getFileName());
			return true;
		}catch (Exception e) {
			getLogger().severe("An error occured while creating the backup.");
			e.printStackTrace();
			return false;
		}
	}

	public boolean createQuestBackup(File file, String id, String msg){
		getLogger().info(msg + " Creating backup...");
		try{
			getLogger().info("Quest backup created at " + Files.copy(file.toPath(), new File(saveFolder, id + "-backup" + format.format(new Date()) + ".yml").toPath()).getFileName());
			return true;
		}catch (Exception e) {
			getLogger().severe("An error occured while creating the backup.");
			e.printStackTrace();
			return false;
		}
	}

	private SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'-'hh'-'mm'-'ss");
	private File backupDir(){
		File f = new File(getDataFolder(), "backup-" + format.format(new Date()));
		if (!f.exists()) f.mkdir();
		return f;
	}
	
	/* ---------- Various quests-related methods ---------- */
	
	public void performReload(CommandSender sender){
		try {
			sender.sendMessage("§c§l-- ⚠ Warning ! This command can occur §omuch§r§c§l bugs ! --");
			sender.sendMessage("§a " + saveAllConfig(true) + " quests saved");
			resetDatas();
		} catch (Throwable e) {
			sender.sendMessage("§cError when saving datas. §lInterrupting operation!");
			e.printStackTrace();
			return;
		}
		
		try{
			reloadConfig();
			loadConfigParameters(false);
			sender.sendMessage("§a Configuration parameters has been reloadeds.");
		}catch (Throwable e){
			sender.sendMessage("§cError when reloading configuration parameters. §lInterrupting operation!");
			e.printStackTrace();
			return;
		}
		
		sender.sendMessage("§7...Waiting for loading quests...");
		new BukkitRunnable() {
			public void run() {
				try {
					data = YamlConfiguration.loadConfiguration(dataFile);
					sender.sendMessage("§a " + loadAllDatas() + " quests loaded");
					sender.sendMessage("§a§lPlugin entierely reloaded from files !");
				} catch (Throwable e) {
					sender.sendMessage("§cError when loading the data file. §lOperation failed!");
					e.printStackTrace();
				}
			}
		}.runTaskLater(BeautyQuests.getInstance(), 20L);
	}
	
	void removeQuest(Quest quest){
		quests.remove(quest);
		NPCStarter starter = npcs.get(quest.getStarter());
		starter.removeQuest(quest);
		if (starter.getQuests().isEmpty()) starter.delete();
	}

	public void addQuest(Quest quest){
		quests.add(quest);
		NPCStarter starter = null;
		if (!npcs.containsKey(quest.getStarter())) {
			starter = new NPCStarter(quest.getStarter());
			npcs.put(quest.getStarter(), starter);
		}else starter = npcs.get(quest.getStarter());
		starter.addQuest(quest);
		quest.create();
	}

	public List<Quest> getQuests(){
		return quests;
	}

	public Map<NPC, NPCStarter> getNPCs(){
		return npcs;
	}
	
	public FileConfiguration getDataFile(){
		return data;
	}
	
	public ScoreboardManager getScoreboardManager(){
		return scoreboards;
	}
	

	public static BeautyQuests getInstance(){
		return instance;
	}
	
}
