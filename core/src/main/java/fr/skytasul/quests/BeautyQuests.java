package fr.skytasul.quests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.tchristofferson.configupdater.ConfigUpdater;

import fr.skytasul.quests.api.Locale;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.bossbar.BQBossBarImplementation;
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
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestsManager;
import fr.skytasul.quests.structure.pools.QuestPoolsManager;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.compatibility.mobs.BukkitEntityFactory;
import fr.skytasul.quests.utils.logger.ILoggerHandler;
import fr.skytasul.quests.utils.logger.LoggerExpanded;
import fr.skytasul.quests.utils.logger.LoggerHandler;
import fr.skytasul.quests.utils.nms.NMS;

public class BeautyQuests extends JavaPlugin {

	public static LoggerExpanded logger;
	private static BeautyQuests instance;
	private BukkitRunnable saveTask;
	private boolean isPaper;
	
	/* --------- Storage --------- */
	
	private String lastVersion;
	private QuestsConfiguration config;

	private String loadedLanguage;

	private Database db;

	private YamlConfiguration data;
	private File dataFile;
	private File saveFolder;
	
	/* --------- Datas --------- */

	private ScoreboardManager scoreboards;
	private QuestsManager quests;
	private QuestPoolsManager pools;
	
	/* ---------- Operations -------- */

	private static boolean disable = false;
	public static boolean loadingFailure = false;
	public static boolean savingFailure = false;
	public static boolean loaded = false;
	
	public DependenciesManager dependencies = new DependenciesManager();
	
	private LoggerHandler loggerHandler;
	
	/* ---------------------------------------------- */

	@Override
	public void onLoad(){
		instance = this;
		logger = new LoggerExpanded(getLogger());
		try{
			if (!getDataFolder().exists()) getDataFolder().mkdir();
			loggerHandler = new LoggerHandler(this);
			getLogger().addHandler(loggerHandler);
		}catch (Throwable ex){
			getLogger().log(Level.SEVERE, "Failed to insert logging handler.", ex);
		}
	}
	
	@Override
	public void onEnable(){
		try {
			logger.info("------------ BeautyQuests ------------");
			
			checkPaper();
			
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
				logger.severe("An error occurred while initializing compatibilities. Consider restarting.", ex);
			}
			
			if (QuestsAPI.getNPCsManager() == null) {
				throw new LoadingException("No NPC plugin installed - please install Citizens or znpcs");
			}
			
			// Launch loading task
			String pluginVersion = getDescription().getVersion();
			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						long lastMillis = System.currentTimeMillis();
						loadAllDatas();
						getLogger().info(quests.getQuestsAmount() + " quests loaded ("
								+ (((double) System.currentTimeMillis() - lastMillis) / 1000D) + "s)!");

						getServer().getPluginManager().registerEvents(new QuestsListener(), BeautyQuests.this);
						
						launchSaveCycle();

						if (!lastVersion.equals(pluginVersion)) { // maybe change in data structure : update of all quest files
							DebugUtils.logMessage("Migrating from " + lastVersion + " to " + pluginVersion);
							int updated = 0;
							for (Quest qu : quests) {
								if (qu.saveToFile()) updated++;
							}
							if (updated > 0) logger.info("Updated " + updated + " quests during migration.");
							saveAllConfig(false);
						}
					}catch (Throwable e) {
						logger.severe("An error occurred while loading plugin datas.", e);
					}
				}
			}.runTaskLater(this, QuestsAPI.getNPCsManager().getTimeToWaitForNPCs());

			// Start of non-essential systems
			if (loggerHandler != null) loggerHandler.launchFlushTimer();
			launchMetrics(pluginVersion);
			try {
				launchUpdateChecker(pluginVersion);
			}catch (Exception e) {
				logger.severe("An error occurred while checking updates.", e);
			}
		}catch (LoadingException ex) {
			if (ex.getCause() != null) logger.severe("A fatal error occurred while loading plugin.", ex.getCause());
			logger.severe(ex.loggerMessage);
			logger.severe("This is a fatal error. Now disabling.");
			disable = true;
			setEnabled(false);
		}
	}
	
	@Override
	public void onDisable(){
		try {
			try {
				Editor.leaveAll();
				Inventories.closeAll();
				stopSaveCycle();
			}catch (Throwable ex) {
				logger.severe("An error occurred while disabling editing systems.", ex);
			}
			
			try {
				if (!disable) saveAllConfig(true);
			}catch (Exception e) {
				logger.severe("An error occurred while saving config.", e);
			}
			try {
				dependencies.disableCompatibilities();
			}catch (Exception e) {
				logger.severe("An error occurred while disabling plugin integrations.", e);
			}
			
			getServer().getScheduler().cancelTasks(this);
		}finally {
			if (loggerHandler != null) loggerHandler.close();
		}
	}
	
	/* ---------- Various init ---------- */
	
	private void checkPaper() {
		try {
			isPaper = Class.forName("com.destroystokyo.paper.ParticleBuilder") != null;
			DebugUtils.logMessage("Paper detected.");
		}catch (ClassNotFoundException ex) {
			isPaper = false;
			logger.warning("You are not running the Paper software.\n"
					+ "It is highly recommended to use it for extended features and more stability.");
		}
	}

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
						logger.severe("Error when saving!", e);
					}
				}
			};
			logger.info("Periodic saves task started (" + cycle + " ticks). Task ID: " + saveTask.runTaskTimerAsynchronously(this, cycle, cycle).getTaskId());
		}
	}
	
	private void stopSaveCycle(){
		if (QuestsConfiguration.saveCycle > 0 && saveTask != null){
			saveTask.cancel();
			saveTask = null;
			logger.info("Periodic saves task stopped.");
		}
	}
	
	private void launchMetrics(String pluginVersion) {
		Metrics metrics = new Metrics(this, 7460);
		metrics.addCustomChart(new DrilldownPie("customPluginVersion", () -> {
			Map<String, Map<String, Integer>> map = new HashMap<>();
			String version = pluginVersion;
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
		metrics.addCustomChart(new SingleLineChart("quests", () -> quests.getQuestsAmount()));
		metrics.addCustomChart(new SimplePie("quests_amount_slice", () -> {
			int size = quests.getQuestsAmount();
			if (size > 200) return "> 200";
			if (size > 100) return "100 - 200";
			if (size > 50) return "50 - 100";
			if (size > 10) return "10 - 50";
			if (size > 5) return "5 - 10";
			return "0 - 5";
		}));
		DebugUtils.logMessage("Started bStats metrics");
	}
	
	private void launchUpdateChecker(String pluginVersion) {
		DebugUtils.logMessage("Starting Spigot updater");
		UpdateChecker checker;
		if (pluginVersion.contains("_")) {
			Matcher matcher = Pattern.compile("_BUILD(\\d+)").matcher(pluginVersion);
			if (matcher.find()) {
				String build = matcher.group(1);
				checker = new UpdateChecker(this, UpdateCheckSource.CUSTOM_URL, "https://ci.codemc.io/job/SkytAsul/job/BeautyQuests/lastSuccessfulBuild/buildNumber")
						.setUserAgent("")
						.setDownloadLink("https://ci.codemc.io/job/SkytAsul/job/BeautyQuests")
						.setUsedVersion(build)
						.setNameFreeVersion("(dev builds)");
			}else {
				logger.warning("Unknown plugin version, cannot check for updates.");
				return;
			}
		}else {
			checker = new UpdateChecker(this, UpdateCheckSource.SPIGOT, "39255")
					.setDownloadLink(39255);
		}
		checker
				.setDonationLink("https://paypal.me/SkytAsul")
				.setSupportLink("https://discord.gg/H8fXrkD")
				.setNotifyOpsOnJoin(false)
				.checkNow();
	}
	
	/* ---------- YAML ---------- */
	
	private void loadConfigParameters(boolean init) throws LoadingException {
		try{
			File configFile = new File(getDataFolder(), "config.yml");
			config = new QuestsConfiguration(this);
			if (config.update()) {
				config.getConfig().save(configFile);
				logger.info("Updated config.");
			}
			if (init) loadLang();
			ConfigUpdater.update(this, "config.yml", configFile);
			config.init();
			
			ConfigurationSection dbConfig = config.getConfig().getConfigurationSection("database");
			if (dbConfig.getBoolean("enabled")) {
				try {
					db = new Database(dbConfig);
					db.testConnection();
					logger.info("Connection to database etablished.");
				}catch (Exception ex) {
					if (db != null) {
						db.closeConnection();
						db = null;
					}
					throw new LoadingException("Connection to database has failed.", ex);
				}
			}
			
			/*				static initialization				*/
			if (init) {
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
			loadedLanguage = config.getConfig().getString("lang", "en_US");
			return Locale.loadLang(this, Lang.values(), loadedLanguage, "en_US", "fr_FR", "zh_CN", "zh_HK", "de_DE", "pt_PT", "it_IT", "es_ES", "sv_SE", "hu_HU", "ru_RU", "pl_PL", "th_TH", "lt_LT", "vi_VN");
		}catch (Exception ex) {
			throw new LoadingException("Couldn't load language file.", ex);
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
				logger.info("You are using a new version for the first time. (last version: " + lastVersion + ")");
				createFolderBackup();
				createDataBackup();
			}
		}else lastVersion = getDescription().getVersion();
		data.options().header("Do not edit ANYTHING here.");
		data.options().copyHeader(true);
	}
	
	private void loadAllDatas() throws Throwable {
		if (disable) return;
		dependencies.lockDependencies();
		
		if (scoreboards == null && QuestsConfiguration.showScoreboards()) {
			File scFile = new File(getDataFolder(), "scoreboard.yml");
			if (!scFile.exists()) saveResource("scoreboard.yml", true);
			scoreboards = new ScoreboardManager(scFile);
			QuestsAPI.registerQuestsHandler(scoreboards);
		}

		try{
			PlayersManager.manager = db == null ? new PlayersManagerYAML() : new PlayersManagerDB(db);
			PlayersManager.manager.load();
		}catch (Exception ex) {
			createDataBackup();
			logger.severe("Error while loading player datas.", ex);
		}
		
		QuestsAPI.getQuestsHandlers().forEach(handler -> {
			try {
				handler.load();
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		
		pools = new QuestPoolsManager(new File(getDataFolder(), "questPools.yml"));
		quests = new QuestsManager(this, data.getInt("lastID"), saveFolder);
		
		if (QuestsConfiguration.firstQuestID != -1) {
			logger.warning("The config option \"firstQuest\" is present in your config.yml but is now unsupported. Please remove it.");
			Quest quest = quests.getQuest(QuestsConfiguration.firstQuestID);
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
	}

	public void saveAllConfig(boolean unload) throws Exception {
		if (unload) {
			if (quests != null) quests.unloadQuests();
			
			QuestsAPI.getQuestsHandlers().forEach(handler -> {
				try {
					handler.unload();
				}catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		}
		
		if (loaded) {
			long time = System.currentTimeMillis();
			data.set("lastID", quests.getLastID());
			data.set("version", getDescription().getVersion());
			
			try {
				PlayersManager.manager.save();
			}catch (Exception ex) {
				createDataBackup();
				logger.severe("Error when saving player datas.", ex);
			}
			data.save(dataFile);
			DebugUtils.logMessage("Saved datas (" + (((double) System.currentTimeMillis() - time) / 1000D) + "s)!");
		}
		
		if (unload){
			QuestsAPI.getNPCsManager().unload();
			resetDatas();
		}
	}
	
	private void resetDatas(){
		quests = null;
		pools = null;
		if (db != null) db.closeConnection();
		//HandlerList.unregisterAll(this);
		loaded = false;
	}
	
	/* ---------- Backups ---------- */
	
	public boolean createFolderBackup() {
		if (!QuestsConfiguration.backups) return false;
		logger.info("Creating quests backup...");
		Path backupDir = backupDir();
		Path saveFolderPath = saveFolder.toPath();
		try (Stream<Path> stream = Files.walk(saveFolderPath)) {
			stream.forEach(path -> {
				try {
					Files.copy(path, backupDir.resolve(saveFolderPath.relativize(path)));
				}catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
			logger.info("Quests backup created in " + backupDir.getFileName().toString());
			return true;
		}catch (Exception e) {
			logger.severe("An error occured while creating the backup.", e);
			return false;
		}
	}
	
	public boolean createDataBackup() {
		if (!QuestsConfiguration.backups) return false;
		logger.info("Creating data backup...");
		try{
			logger.info("Datas backup created in " + Files.copy(dataFile.toPath(), backupDir().resolve("data.yml")).getParent().getFileName());
			return true;
		}catch (Exception e) {
			logger.severe("An error occured while creating the backup.", e);
			return false;
		}
	}

	public boolean createQuestBackup(Path file, String msg) {
		if (!QuestsConfiguration.backups) return false;
		logger.info("Creating single quest backup...");
		try{
			logger.info("Quest backup created at " + Files.copy(file, Paths.get(file.toString() + "-backup" + format.format(new Date()) + ".yml")).getFileName());
			return true;
		}catch (Exception e) {
			logger.severe("An error occured while creating the backup.", e);
			return false;
		}
	}

	private SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'-'hh'-'mm'-'ss");
	
	private Path backupDir() {
		return getDataFolder().toPath().resolve("backup-" + format.format(new Date()));
	}
	
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
					loadAllDatas();
					sender.sendMessage("§a " + quests.getQuestsAmount() + " quests loaded");
					sender.sendMessage("§a§lPlugin entierely reloaded from files !");
				} catch (Throwable e) {
					sender.sendMessage("§cError when loading the data file. §lOperation failed!");
					e.printStackTrace();
				}
			}
		}.runTaskLater(BeautyQuests.getInstance(), 20L);
	}
	
	public QuestsConfiguration getConfiguration() {
		return config;
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
	
	public QuestsManager getQuestsManager() {
		return quests;
	}
	
	public QuestPoolsManager getPoolsManager() {
		return pools;
	}
	
	public ILoggerHandler getLoggerHandler() {
		return loggerHandler == null ? ILoggerHandler.EMPTY_LOGGER : loggerHandler;
	}
	
	public boolean isRunningPaper() {
		return isPaper;
	}


	public static BeautyQuests getInstance(){
		return instance;
	}
	
	public static class LoadingException extends Exception {
		private static final long serialVersionUID = -2811265488885752109L;

		private String loggerMessage;

		public LoadingException(String loggerMessage) {
			this.loggerMessage = loggerMessage;
		}

		public LoadingException(String loggerMessage, Throwable cause) {
			super(cause);
			this.loggerMessage = loggerMessage;
		}
		
		public String getLoggerMessage() {
			return loggerMessage;
		}
		
	}
	
}
