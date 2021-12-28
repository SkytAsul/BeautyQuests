package fr.skytasul.quests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
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
import com.tchristofferson.configupdater.ConfigUpdater;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.bossbar.BQBossBarImplementation;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.commands.Commands;
import fr.skytasul.quests.commands.CommandsManager;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.FinishGUI;
import fr.skytasul.quests.gui.creation.QuestObjectGUI;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.misc.ItemComparisonGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.options.OptionAutoQuest;
import fr.skytasul.quests.options.OptionStarterNPC;
import fr.skytasul.quests.players.PlayerAccount;
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
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.mobs.BukkitEntityFactory;
import fr.skytasul.quests.utils.nms.NMS;

public class BeautyQuests extends JavaPlugin {

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
	private Map<BQNPC, NPCStarter> npcs = new HashMap<>();
	private ScoreboardManager scoreboards;
	private QuestPoolsManager pools;
	public static int lastID = 0;
	
	/* ---------- Operations -------- */

	private static boolean disable = false;
	public static boolean loadingFailure = false;
	public static boolean savingFailure = false;
	public static boolean loaded = false;
	
	public DependenciesManager dependencies = new DependenciesManager();
	
	/* ---------------------------------------------- */

	@Override
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
			ex.printStackTrace();
		}
		if (logger == null || !logger.isEnabled()) System.out.println("Failed to insert new logger to BeautyQuests. Current logger: " + getLogger().getClass().getName());
	}
	
	@Override
	public void onEnable(){
		try {
			dependencies.testCompatibilities();
			Bukkit.getPluginManager().registerEvents(dependencies, this);

			saveDefaultConfig();
			NMS.getMCVersion();
			registerCommands();

			saveFolder = new File(getDataFolder(), "quests");
			if (!saveFolder.exists()) saveFolder.mkdirs();
			loadDataFile();
			loadConfigParameters(true);

			try {
				dependencies.initializeCompatibilities();
			}catch (Exception ex) {
				logger.severe("Error when initializing compatibilities. Consider restarting.");
				ex.printStackTrace();
			}
			
			if (QuestsAPI.getNPCsManager() == null) {
				throw new LoadingException("No NPC plugin installed - please install Citizens or znpcs");
			}
			
			// Launch loading task
			new BukkitRunnable() {
				@Override
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
			}.runTaskLater(this, QuestsAPI.getNPCsManager().getTimeToWaitForNPCs());
			

			// Start of non-essential systems
			logger.launchFlushTimer();
			try {
				new SpigotUpdater(this, 39255);
				DebugUtils.logMessage("Started Spigot updater");
			}catch (IOException e1) {
				e1.printStackTrace();
			}

			Metrics metrics = new Metrics(this, 7460);
			metrics.addCustomChart(new DrilldownPie("customPluginVersion", () -> {
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
			metrics.addCustomChart(new SimplePie("lang", () -> loadedLanguage));
			metrics.addCustomChart(new SimplePie("storage", () -> db == null ? "YAML (files)" : "SQL (database)"));
			metrics.addCustomChart(new SingleLineChart("quests", () -> quests.size()));
			metrics.addCustomChart(new SimplePie("quests_amount_slice", () -> {
				int size = quests.size();
				if (size > 200) return "> 200";
				if (size > 100) return "100 - 200";
				if (size > 50) return "50 - 100";
				if (size > 10) return "10 - 50";
				if (size > 5) return "5 - 10";
				return "0 - 5";
			}));
			DebugUtils.logMessage("Started bStats metrics");
		}catch (LoadingException ex) {
			if (ex.getCause() != null) ex.getCause().printStackTrace();
			logger.severe(ex.loggerMessage);
			logger.severe("This is a fatal error. Now disabling.");
			disable = true;
			setEnabled(false);
		}
	}

	@Override
	public void onDisable(){
		try {
			Editor.leaveAll();
			Inventories.closeAll();
			stopSaveCycle();
			
			try {
				if (!disable) saveAllConfig(true);
			}catch (Exception e) {
				e.printStackTrace();
			}
			try {
				dependencies.disableCompatibilities();
			}catch (Exception e) {
				e.printStackTrace();
			}
			
			getServer().getScheduler().cancelTasks(this);
		}finally {
			if (logger != null) logger.close();
		}
	}
	
	/* ---------- Various init ---------- */

	private void registerCommands(){
		CommandsManager questCommand = new CommandsManager((sender) -> {
			if (!(sender instanceof Player)) return;
			Player p = (Player) sender;
			if (!p.hasPermission("beautyquests.command.listPlayer")){
				Lang.INCORRECT_SYNTAX.send(p);
			}else {
				PlayerAccount acc = PlayersManager.getPlayerAccount(p);
				if (acc == null) {
					Lang.ERROR_OCCURED.send(p, "no account data");
					logger.severe("Player " + p.getName() + " has got no account. This is a CRITICAL issue.");
				}else Inventories.create(p, new PlayerListGUI(acc));
			}
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
				@Override
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
			ConfigUpdater.update(this, "config.yml", new File(getDataFolder(), "config.yml"));
			
			QuestsConfiguration.initConfiguration(config);
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
				ItemComparisonGUI.initialize();
				QuestsAPI.registerMobFactory(new BukkitEntityFactory());
				if (NMS.getMCVersion() >= 9) QuestsAPI.setBossBarManager(new BQBossBarImplementation());
			}
		}catch (LoadingException ex) {
			throw ex;
		}catch (Throwable ex) {
			throw new LoadingException("Error while loading configuration and initializing values", ex);
		}
	}
	
	private YamlConfiguration loadLang() throws LoadingException {
		try {
			for (String language : new String[] { "en_US", "fr_FR", "zh_CN", "zh_HK", "de_DE", "pt_PT", "it_IT", "es_ES", "sv_SE", "hu_HU", "ru_RU", "pl_PL", "th_TH", "lt_LT", "vi_VN" }) {
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

			if (changes) {
				getLogger().info("Copied new strings into " + language + " language file.");
				conf.save(file); // if there has been changes before, save the edited file
			}
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
		dependencies.lockDependencies();
		
		if (scoreboards == null && QuestsConfiguration.showScoreboards()) {
			File scFile = new File(getDataFolder(), "scoreboard.yml");
			if (!scFile.exists()) saveResource("scoreboard.yml", true);
			scoreboards = new ScoreboardManager(scFile);
			QuestsAPI.registerQuestsHandler(scoreboards);
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
		
		QuestsAPI.getQuestsHandlers().forEach(handler -> {
			try {
				handler.load();
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		try (Stream<Path> files = Files.walk(saveFolder.toPath(), Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)){
			files
				.filter(Files::isRegularFile)
				.filter(path -> !path.getFileName().toString().contains("backup"))
				.filter(path -> "yml".equalsIgnoreCase(Utils.getFilenameExtension(path.getFileName().toString()).orElse(null))).forEach(path -> {
					loadingFailure = false;
					try {
						File file = path.toFile();
						Quest quest = Quest.loadFromFile(file);
						if (quest != null) {
							addQuest(quest);
							if (loadingFailure) createQuestBackup(path, "Error when loading quest.");
						}else logger.severe("Quest from file " + file.getName() + " not activated");
					}catch (Exception ex) {
						ex.printStackTrace();
					}
				});
		}
		if (QuestsConfiguration.firstQuestID != -1) {
			logger.warning("The config option \"firstQuest\" is present in your config.yml but is now unsupported. Please remove it.");
			Quest quest = QuestsAPI.getQuestFromID(QuestsConfiguration.firstQuestID);
			if (quest != null) {
				if (quest.hasOption(OptionAutoQuest.class)) {
					OptionAutoQuest option = quest.getOption(OptionAutoQuest.class);
					if (!option.getValue()) {
						option.setValue(true);
						quest.saveToFile();
					}
				}else {
					OptionAutoQuest option = new OptionAutoQuest();
					option.setValue(true);
					quest.addOption(option);
					quest.saveToFile();
				}
			}
		}
		
		Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayersManager.loadPlayer(p);
			}
			loaded = true;
		}, 1L);
		
		
		return quests.size();
	}

	public void saveAllConfig(boolean unload) throws Exception {
		if (unload){
			QuestsAPI.getQuestsHandlers().forEach(handler -> {
				try {
					handler.unload();
				}catch (Exception ex) {
					ex.printStackTrace();
				}
			});
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
			for (Quest quest : quests) {
				try {
					quest.unload();
				}catch (Exception ex) {
					logger.severe("An error ocurred when unloading quest " + quest.getID());
					ex.printStackTrace();
				}
			}
		}
		if (loaded) {
			data.set("lastID", lastID);
			data.set("version", getDescription().getVersion());
			
			try {
				PlayersManager.manager.save();
			}catch (Exception ex) {
				createDataBackup("Error when saving player datas.");
				ex.printStackTrace();
			}
			data.save(dataFile);
		}
		
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
		loaded = false;
	}
	
	/* ---------- Backups ---------- */
	
	public boolean createFolderBackup(String msg){
		if (!QuestsConfiguration.backups) return false;
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
		if (!QuestsConfiguration.backups) return false;
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

	public boolean createQuestBackup(Path file, String msg) {
		if (!QuestsConfiguration.backups) return false;
		getLogger().info(msg + " Creating backup...");
		try{
			getLogger().info("Quest backup created at " + Files.copy(file, Paths.get(file.toString() + "-backup" + format.format(new Date()) + ".yml")).getFileName());
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
			@Override
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
			BQNPC value = quest.getOptionValueOrDef(OptionStarterNPC.class);
			NPCStarter starter = npcs.get(value);
			if (starter == null) {
				logger.warning("NPC Starter not registered for quest " + quest.getID() + ". NPC: " + (value == null ? "not set" : value.getId()));
			}else starter.removeQuest(quest);
		}
	}

	public void addQuest(Quest quest){
		lastID = Math.max(lastID, quest.getID());
		quests.add(quest);
		if (quest.hasOption(OptionStarterNPC.class)) {
			BQNPC npc = quest.getOptionValueOrDef(OptionStarterNPC.class);
			if (npc != null) {
				NPCStarter starter = null;
				if (!npcs.containsKey(npc)) {
					starter = new NPCStarter(npc);
					npcs.put(npc, starter);
				}else starter = npcs.get(npc);
				starter.addQuest(quest);
			}
		}
		quest.load();
	}

	public List<Quest> getQuests(){
		return quests;
	}

	public Map<BQNPC, NPCStarter> getNPCs() {
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
