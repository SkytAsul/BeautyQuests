package fr.skytasul.quests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Charsets;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.commands.Commands;
import fr.skytasul.quests.commands.CommandsManager;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.structure.NPCStarter;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.pools.QuestPoolsManager;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.SpigotUpdater;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.Dynmap;
import fr.skytasul.quests.utils.compatibility.mobs.BukkitEntityFactory;
import fr.skytasul.quests.utils.compatibility.mobs.CitizensFactory;
import fr.skytasul.quests.utils.nms.NMS;
import net.citizensnpcs.api.npc.NPC;

public class BeautyQuests extends JavaPlugin{

	public static QuestsLogger logger;
	private static BeautyQuests instance;
	private BukkitRunnable saveTask;
	
	/* --------- Storage --------- */
	
	private String lastVersion;
	private FileConfiguration config;

	private String loadedLanguage;

	private Database db;

	private YamlConfiguration data;
	private File dataFile;
	public static File saveFolder;
	
	/* --------- Datas --------- */

	private List<Quest> quests = new ArrayList<>();
	private Map<NPC, NPCStarter> npcs = new HashMap<>();
	private ScoreboardManager scoreboards;
	private QuestPoolsManager pools;
	public static int lastID = 0;
	
	/* ---------- Operations -------- */

	private static boolean disable = false;
	public static boolean loadingFailure = false;
	public static boolean savingFailure = false;
	public static boolean loaded = false;
	
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
		try {
			if (!getServer().getPluginManager().isPluginEnabled("Citizens")) {
				throw new LoadingException("Citizens plugin is not installed.");
			}
			DependenciesManager.testCompatibilities();

			new BukkitRunnable() {
				public void run() {
					try {
						long lastMillis = System.currentTimeMillis();
						getLogger().info(loadAllDatas() + " quests loaded ("
								+ (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");

						getServer().getPluginManager().registerEvents(new QuestsListener(), BeautyQuests.this);
						
						launchSaveCycle();

						if (!lastVersion.equals(getDescription().getVersion())) { // maybe change in data structure : update of all quest files
							DebugUtils.logMessage("Migrating from " + lastVersion + " to " + getDescription().getVersion());
							for (Quest qu : quests) qu.saveToFile();
							saveAllConfig(false);
						}
					}catch (Throwable e) {
						e.printStackTrace();
					}
				}
			}.runTaskLater(this, 2L);

			saveDefaultConfig();
			NMS.getMCVersion();
			registerCommands();

			saveFolder = new File(getDataFolder(), "quests");
			if (!saveFolder.exists()) saveFolder.mkdirs();
			loadDataFile();
			loadConfigParameters(true);

			try {
				DependenciesManager.initializeCompatibilities();
			}catch (Throwable ex) {
				logger.severe("Error when initializing compatibilities. Consider restarting.");
				ex.printStackTrace();
			}

			logger.launchFlushTimer();
			try {
				new SpigotUpdater(this, 39255);
				DebugUtils.logMessage("Started Spigot updater");
			}catch (IOException e1) {
				e1.printStackTrace();
			}

			Metrics metrics = new Metrics(this, 7460);
			metrics.addCustomChart(new Metrics.DrilldownPie("customPluginVersion", () -> {
				Map<String, Map<String, Integer>> map = new HashMap<>();
				String version = getDescription().getVersion();
				Map<String, Integer> entry = new HashMap<>();
				String[] split = version.split("_");
				if (split.length == 1) {
					entry.put("Release", 1);
				}else {
					entry.put(version, 1);
				}
				map.put(split[0], entry);
				return map;
			}));
			metrics.addCustomChart(new Metrics.SimplePie("lang", () -> loadedLanguage));
			metrics.addCustomChart(new Metrics.SimplePie("storage", () -> db == null ? "YAML (files)" : "SQL (database)"));
			metrics.addCustomChart(new Metrics.SingleLineChart("quests", () -> quests.size()));
			DebugUtils.logMessage("Started bStats metrics");
		}catch (LoadingException ex) {
			if (ex.getCause() != null) ex.getCause().printStackTrace();
			logger.severe(ex.loggerMessage);
			logger.severe("This is a fatal error. Now disabling.");
			disable = true;
			setEnabled(false);
		}
	}

	public void onDisable(){
		Editor.leaveAll();
		Inventories.closeAll();
		getServer().getScheduler().cancelTasks(this);
		stopSaveCycle();
		
		try {
			if (!disable) saveAllConfig(true);
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
		if (QuestsConfiguration.saveCycle > 0 && saveTask == null) {
			int cycle = QuestsConfiguration.saveCycle * 60 * 20;
			saveTask = new BukkitRunnable() {
				public void run() {
					try {
						saveAllConfig(false);
						if (QuestsConfiguration.saveCycleMessage) logger.info("Datas saved ~ periodic save");
					}catch (Exception e) {
						logger.severe("Error when saving!");
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
	
	private void loadConfigParameters(boolean init) throws LoadingException {
		try{
			config = getConfig();
			
			try {
				QuestsConfiguration.initConfiguration(config);
			}catch (Exception ex) {
				throw new LoadingException("An error occured while loading config parameters.", ex);
			}
			ConfigurationSection dbConfig = config.getConfigurationSection("database");
			if (dbConfig.getBoolean("enabled")) {
				db = new Database(dbConfig);
				if (db.openConnection()) {
					logger.info("Connection to database etablished.");
				}else {
					db.closeConnection();
					db = null;
					throw new LoadingException("Connection to database has failed.");
				}
			}
			
			/*				static initialization				*/
			if (init) {
				if (loadLang() == null) return;
				StagesGUI.initialize(); // 			initializing default stage types
				QuestObjectGUI.initialize(); //			initializing default rewards and requirements
				FinishGUI.initialize(); //				initializing default quest options
				QuestsAPI.registerMobFactory(new BukkitEntityFactory());
				QuestsAPI.registerMobFactory(new CitizensFactory());
			}
		}catch (LoadingException ex) {
			throw ex;
		}catch (Exception ex){
			getLogger().severe("Error when loading.");
			ex.printStackTrace();
		}
	}
	
	private YamlConfiguration loadLang() throws LoadingException {
		try {
			for (String language : new String[] { "en_US", "fr_FR", "zh_CN", "zh_HK", "de_DE", "pt_PT", "it_IT", "es_ES", "sv_SE", "hu_HU", "ru_RU", "pl_PL", "th_TH" }) {
				File file = new File(getDataFolder(), "locales/" + language + ".yml");
				if (!file.exists()) saveResource("locales/" + language + ".yml", false);
			}

			long lastMillis = System.currentTimeMillis();
			loadedLanguage = config.getString("lang", "en_US");
			String language = "locales/" + loadedLanguage + ".yml";
			File file = new File(getDataFolder(), language);
			InputStream res = getResource(language);
			boolean created = false;
			if (!file.exists()){
				logger.warning("Language file " + language + " does not exist. Using default english strings.");
				file.createNewFile();
				res = getResource("locales/en_US.yml");
				created = true;
			}
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			boolean changes = false;
			if (res != null){ // if it's a local resource
				YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(res, StandardCharsets.UTF_8));
				for (String key : def.getKeys(true)){ // get all keys in resource
					if (!def.isConfigurationSection(key)){ // if not a block
						if (!conf.contains(key)){ // if string does not exist in the file
							conf.set(key, def.get(key)); // copy string
							if (!created) DebugUtils.logMessage("String copied from source file to " + language + ". Key: " + key);
							changes = true;
						}
					}
				}
			}
			Lang.loadStrings(YamlConfiguration.loadConfiguration(new InputStreamReader(getResource("locales/en_US.yml"), Charsets.UTF_8)), conf);

			if (changes) conf.save(file); // if there has been changes before, save the edited file
			getLogger().info("Loaded language file " + language + " (" + (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");
			return conf;
		} catch(Exception e) {
			throw new LoadingException("Couldn't create language file.", e);
		}
	}
	
	private void loadDataFile() throws LoadingException {
		dataFile = new File(getDataFolder(), "data.yml");
		if (!dataFile.exists()){
			try {
				dataFile.createNewFile();
				getLogger().info("data.yml created.");
			} catch (IOException e) {
				throw new LoadingException("Couldn't create data file.", e);
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
		if (DependenciesManager.dyn){
			try{
				Dynmap.intitialize();
			}catch (Throwable ex){
				getLogger().severe("An error occured while initializing dynmap integration.");
				ex.printStackTrace();
				Dynmap.unload();
			}
		}
		
		pools = new QuestPoolsManager(new File(getDataFolder(), "questPools.yml"));
		
		quests.clear();
		lastID = data.getInt("lastID");

		try{
			PlayersManager.manager = db == null ? new PlayersManagerYAML() : new PlayersManagerDB(db);
			PlayersManager.manager.load();
		}catch (Exception ex) {
			createDataBackup("Error when loading player datas.");
			ex.printStackTrace();
		}

		for (File file : saveFolder.listFiles()) {
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
		
		Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayersManager.loadPlayer(p);
				//getServer().getPluginManager().callEvent(new PlayerAccountJoinEvent(p, PlayersManager.getPlayerAccount(p), false));
			}
			loaded = true;
		}, 1L);
		
		
		return quests.size();
	}

	public void saveAllConfig(boolean unload) throws Exception {
		if (unload){
			if (scoreboards != null) scoreboards.unload();
		}
		
		/*int amount = 0; // no longer need to save quests
		for (Quest qu : quests){
			savingFailure = false;
			try{
				qu.saveToFile(unload);
				amount++;
			}catch (Exception ex) {
				getLogger().warning("Error when saving quest ID " + qu.getID());
				ex.printStackTrace();
				continue;
			}
		}*/
		if (unload) {
			quests.forEach(Quest::unloadAll);
		}
		data.set("lastID", lastID);
		data.set("version", getDescription().getVersion());
		
		try{
			PlayersManager.manager.save();
		}catch (Throwable ex){
			createDataBackup("Error when saving player datas.");
			ex.printStackTrace();
		}
		data.save(dataFile);
		
		if (unload){
			resetDatas();
		}
	}
	
	private void resetDatas(){
		npcs.values().forEach(NPCStarter::removeHolograms);
		quests.clear();
		npcs.clear();
		if (db != null) db.closeConnection();
		//HandlerList.unregisterAll(this);
		if (DependenciesManager.dyn) Dynmap.unload();
		loaded = false;
	}
	
	/* ---------- Backups ---------- */
	
	public boolean createFolderBackup(String msg){
		getLogger().info(msg + " Creating backup...");
		try {
			File backupDir = backupDir();
			backupDir.mkdir();
			for (File file : saveFolder.listFiles()) {
				Files.copy(file.toPath(), new File(backupDir, file.getName()).toPath());
			}
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
			saveAllConfig(true);
			sender.sendMessage("§aDatas saved!");
		}catch (Exception e) {
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
	
	public void removeQuest(Quest quest){
		quests.remove(quest);
		if (quest.hasOption(OptionStarterNPC.class)) {
			NPC value = quest.getOptionValueOrDef(OptionStarterNPC.class);
			NPCStarter starter = npcs.get(value);
			if (starter == null) {
				logger.warning("NPC Starter not registered for quest " + quest.getID() + ". NPC: " + (value == null ? "not set" : value.getId()));
			}else starter.removeQuest(quest);
		}
	}

	public void addQuest(Quest quest){
		quests.add(quest);
		if (quest.hasOption(OptionStarterNPC.class)) {
			NPC npc = quest.getOptionValueOrDef(OptionStarterNPC.class);
			if (npc != null) {
				NPCStarter starter = null;
				if (!npcs.containsKey(npc)) {
					starter = new NPCStarter(npc);
					npcs.put(npc, starter);
				}else starter = npcs.get(npc);
				starter.addQuest(quest);
			}
		}
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
	
	public Database getBQDatabase() {
		return db;
	}

	public ScoreboardManager getScoreboardManager(){
		return scoreboards;
	}
	
	public QuestPoolsManager getPoolsManager() {
		return pools;
	}


	public static BeautyQuests getInstance(){
		return instance;
	}
	
	class LoadingException extends Exception {
		private static final long serialVersionUID = -2811265488885752109L;

		private String loggerMessage;

		public LoadingException(String loggerMessage) {
			this.loggerMessage = loggerMessage;
		}

		public LoadingException(String loggerMessage, Throwable cause) {
			super(cause);
			this.loggerMessage = loggerMessage;
		}
	}
	
}
