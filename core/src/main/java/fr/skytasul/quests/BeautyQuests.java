package fr.skytasul.quests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.tchristofferson.configupdater.ConfigUpdater;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsAPIProvider;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.EditorManager;
import fr.skytasul.quests.api.gui.GuiManager;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.localization.Locale;
import fr.skytasul.quests.api.utils.IntegrationManager;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.commands.CommandsManagerImplementation;
import fr.skytasul.quests.editor.EditorManagerImplementation;
import fr.skytasul.quests.gui.GuiManagerImplementation;
import fr.skytasul.quests.npcs.BqNpcManagerImplementation;
import fr.skytasul.quests.options.OptionAutoQuest;
import fr.skytasul.quests.players.AbstractPlayersManager;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.structure.QuestImplementation;
import fr.skytasul.quests.structure.QuestsManagerImplementation;
import fr.skytasul.quests.structure.pools.QuestPoolsManagerImplementation;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.compatibility.InternalIntegrations;
import fr.skytasul.quests.utils.compatibility.Post1_16;
import fr.skytasul.quests.utils.logger.LoggerHandler;
import fr.skytasul.quests.utils.nms.NMS;

public class BeautyQuests extends JavaPlugin implements QuestsPlugin {

	private static BeautyQuests instance;
	private BukkitRunnable saveTask;
	private boolean isPaper;

	/* --------- Storage --------- */

	private String lastVersion;
	private QuestsConfigurationImplementation config;

	private String loadedLanguage;

	private Database db;

	private YamlConfiguration data;
	private File dataFile;
	private File saveFolder;

	private Path backupDir = null;

	/* --------- Datas --------- */

	private final @NotNull BqNpcManagerImplementation npcManager = new BqNpcManagerImplementation();
	private @Nullable ScoreboardManager scoreboards;
	private @Nullable QuestsManagerImplementation quests;
	private @Nullable QuestPoolsManagerImplementation pools;
	private @Nullable AbstractPlayersManager players;

	/* ---------- Operations -------- */

	private boolean disable = false;
	protected boolean loadingFailure = false;
	protected boolean savingFailure = false;
	protected boolean loaded = false;

	private @NotNull IntegrationManager integrations = new IntegrationManager();
	private @Nullable CommandsManagerImplementation command;
	private @Nullable LoggerExpanded logger;
	private @Nullable LoggerHandler loggerHandler;
	private @Nullable GuiManagerImplementation guiManager;
	private @Nullable EditorManagerImplementation editorManager;

	/* ---------------------------------------------- */

	@Override
	public void onLoad(){
		instance = this;

		loggerHandler = null;
		try{
			if (!getDataFolder().exists()) getDataFolder().mkdir();
			loggerHandler = new LoggerHandler(this);
			getLogger().addHandler(loggerHandler);
		}catch (Throwable ex){
			getLogger().log(Level.SEVERE, "Failed to insert logging handler.", ex);
		}

		logger = new LoggerExpanded(getLogger(), loggerHandler);

		try {
			initApi();
		} catch (Exception ex) {
			logger.severe("An unexpected exception occurred while initializing the API.", ex);
			logger.severe("This is a fatal error. Now disabling.");
			disable = true;
			setEnabled(false);
		}
	}

	@Override
	public void onEnable(){
		if (disable)
			return;

		try {
			logger.info("------------ BeautyQuests ------------");

			checkPaper();

			loadDefaultIntegrations();
			integrations.testCompatibilities();
			Bukkit.getPluginManager().registerEvents(integrations, this);

			saveDefaultConfig();
			NMS.isValid(); // to force initialization

			saveFolder = new File(getDataFolder(), "quests");
			if (!saveFolder.exists()) saveFolder.mkdirs();
			loadDataFile();
			loadConfigParameters(true);
			checkLastVersion();

			registerCommands();

			try {
				integrations.initializeCompatibilities();
			}catch (Exception ex) {
				logger.severe("An error occurred while initializing compatibilities. Consider restarting.", ex);
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
						if (MinecraftVersion.MAJOR >= 16)
							getServer().getPluginManager().registerEvents(new Post1_16(), BeautyQuests.this);

						launchSaveCycle();

						if (!lastVersion.equals(pluginVersion)) { // maybe change in data structure : update of all quest files
							QuestsPlugin.getPlugin().getLoggerExpanded().debug("Migrating from " + lastVersion + " to " + pluginVersion);
							int updated = quests.updateAll();
							if (updated > 0) logger.info("Updated " + updated + " quests during migration.");
							pools.updateAll();
							saveAllConfig(false);
						}
					}catch (Throwable e) {
						logger.severe("An error occurred while loading plugin datas.", e);
					}
				}
			}.runTaskLater(this, npcManager.getTimeToWaitForNPCs());

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
		}catch (Exception ex) {
			logger.severe("An unexpected exception occurred while loading plugin.", ex);
			logger.severe("This is a fatal error. Now disabling.");
			disable = true;
			setEnabled(false);
		}
	}

	@Override
	public void onDisable(){
		try {
			try {
				if (command != null) command.unload();
			}catch (Throwable ex) {
				logger.severe("An error occurred while disabling command manager.", ex);
			}

			try {
				editorManager.leaveAll();
				guiManager.closeAll();
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
				integrations.disableCompatibilities();
			}catch (Exception e) {
				logger.severe("An error occurred while disabling plugin integrations.", e);
			}

			getServer().getScheduler().cancelTasks(this);
		}finally {
			if (loggerHandler != null) loggerHandler.close();
		}
	}

	/* ---------- Various init ---------- */

	private void initApi() throws ReflectiveOperationException {
		Method setMethod = QuestsAPIProvider.class.getDeclaredMethod("setAPI", QuestsAPI.class);
		setMethod.setAccessible(true); // NOSONAR
		setMethod.invoke(null, QuestsAPIImplementation.INSTANCE);
	}

	private void checkPaper() {
		try {
			isPaper = Class.forName("com.destroystokyo.paper.ParticleBuilder") != null;
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Paper detected.");
		}catch (ClassNotFoundException ex) {
			isPaper = false;
			logger.warning("You are not running the Paper software.\n"
					+ "It is highly recommended to use it for extended features and more stability.");
		}
	}

	private void registerCommands(){
		command = new CommandsManagerImplementation();
		command.initializeCommands();
		command.lockCommands(); // we are obligated to register Brigadier during plugin initialization...
	}

	private void launchSaveCycle(){
		if (config.saveCycle > 0 && saveTask == null) {
			int cycle = config.saveCycle * 60 * 20;
			saveTask = new BukkitRunnable() {
				@Override
				public void run() {
					try {
						saveAllConfig(false);
						if (config.saveCycleMessage)
							logger.info("Datas saved ~ periodic save");
					}catch (Exception e) {
						logger.severe("Error when saving!", e);
					}
				}
			};
			logger.info("Periodic saves task started (" + cycle + " ticks). Task ID: " + saveTask.runTaskTimerAsynchronously(this, cycle, cycle).getTaskId());
		}
	}

	private void stopSaveCycle(){
		if (config.saveCycle > 0 && saveTask != null) {
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
		metrics.addCustomChart(new AdvancedPie("hooks", () -> { // replace with bar chart when bStats add them back
			return integrations.getDependencies()
				.stream()
				.filter(dep -> dep.isEnabled())
				.map(dep -> dep.getFoundPlugin().getName())
				.distinct()
				.collect(Collectors.toMap(Function.identity(), __ -> 1));
		}));
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Started bStats metrics");
	}

	private void launchUpdateChecker(String pluginVersion) {
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Starting Spigot updater");
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
			config = new QuestsConfigurationImplementation(getConfig());
			if (config.update()) {
				config.getConfig().save(configFile);
				logger.info("Updated config.");
			}
			if (init) loadLang();
			ConfigUpdater.update(this, "config.yml", configFile);
			config.init();

			ConfigurationSection dbConfig = config.getConfig().getConfigurationSection("database");
			if (dbConfig.getBoolean("enabled")) {
				db = null;
				try {
					db = new Database(dbConfig);
					db.testConnection();
					logger.info("Connection to database etablished.");
				}catch (Exception ex) {
					db = null;
					throw new LoadingException("Connection to database has failed.", ex);
				}
			}

			players = db == null ? new PlayersManagerYAML() : new PlayersManagerDB(db);

			/*				static initialization				*/
			if (init) {
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default stage types.");
				DefaultQuestFeatures.registerStages();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default quest options.");
				DefaultQuestFeatures.registerQuestOptions();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default item comparisons.");
				DefaultQuestFeatures.registerItemComparisons();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default rewards.");
				DefaultQuestFeatures.registerRewards();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default requirements.");
				DefaultQuestFeatures.registerRequirements();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default stage options.");
				DefaultQuestFeatures.registerStageOptions();
				QuestsPlugin.getPlugin().getLoggerExpanded().debug("Initializing default miscellenaeous.");
				DefaultQuestFeatures.registerMisc();
				DefaultQuestFeatures.registerMessageProcessors();
				getServer().getPluginManager().registerEvents(guiManager = new GuiManagerImplementation(), this);
				getServer().getPluginManager().registerEvents(editorManager = new EditorManagerImplementation(), this);
			}
		}catch (LoadingException ex) {
			throw ex;
		}catch (Throwable ex) {
			throw new LoadingException("Error while loading configuration and initializing values", ex);
		}
	}


	private void loadDefaultIntegrations() {
		try {
			Class<?> loaderClass = Class.forName("fr.skytasul.quests.integrations.IntegrationsLoader");
			loaderClass.getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException ex) {
			logger.warning("Could not find integrations loader class.");
		} catch (ReflectiveOperationException ex) {
			logger.severe("Cannot load default integrations.", ex);
		}
		InternalIntegrations.AccountsHook.isEnabled(); // to initialize the class
	}

	private YamlConfiguration loadLang() throws LoadingException {
		try {
			loadedLanguage = config.getConfig().getString("lang", "en_US");
			return Locale.loadLang(this, Lang.values(), loadedLanguage);
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
		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Loading data file, last time edited : " + new Date(dataFile.lastModified()).toString());
		data = YamlConfiguration.loadConfiguration(dataFile);
		data.options().header("Do not edit ANYTHING here.");
		data.options().copyHeader(true);
	}

	private void checkLastVersion() {
		if (data.contains("version")){
			lastVersion = data.getString("version");
			if (!lastVersion.equals(getDescription().getVersion())){
				logger.info("You are using a new version for the first time. (last version: " + lastVersion + ")");
				backupDir = backupDir();
				createFolderBackup(backupDir);
				createDataBackup(backupDir);
			}
		}else lastVersion = getDescription().getVersion();
	}

	private void loadAllDatas() throws Throwable {
		if (disable) return;
		integrations.lockDependencies();
		// command.lockCommands(); we cannot register Brigadier after plugin initialization...

		if (scoreboards == null && config.getQuestsConfig().scoreboards()) {
			File scFile = new File(getDataFolder(), "scoreboard.yml");
			if (!scFile.exists()) saveResource("scoreboard.yml", true);
			scoreboards = new ScoreboardManager(scFile);
			getAPI().registerQuestsHandler(scoreboards);
		}

		try{
			if (db == null && backupDir != null)
				createPlayerDatasBackup(backupDir, (PlayersManagerYAML) players);

			players.load();
		}catch (Exception ex) {
			if (backupDir == null) createDataBackup(backupDir());
			logger.severe("Error while loading player datas.", ex);
		}

		getAPI().getQuestsHandlers().forEach(handler -> {
			try {
				handler.load();
			}catch (Exception ex) {
				logger.severe("Cannot load quest handler " + handler.getClass().getName(), ex);
			}
		});

		pools = new QuestPoolsManagerImplementation(new File(getDataFolder(), "questPools.yml"));
		quests = new QuestsManagerImplementation(this, data.getInt("lastID"), saveFolder);

		if (config.firstQuestID != -1) {
			logger.warning("The config option \"firstQuest\" is present in your config.yml but is now unsupported. Please remove it.");
			QuestImplementation quest = quests.getQuest(config.firstQuestID);
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
				players.loadPlayer(p);
			}
			loaded = true;
		}, 1L);
	}

	public void saveAllConfig(boolean unload) throws Exception {
		if (unload) {
			if (quests != null) quests.unloadQuests();

			getAPI().getQuestsHandlers().forEach(handler -> {
				try {
					handler.unload();
				}catch (Exception ex) {
					logger.severe("Cannot unload quest handler " + handler.getClass().getName(), ex);
				}
			});
		}

		if (loaded) {
			long time = System.currentTimeMillis();
			data.set("lastID", quests.getLastID());
			data.set("version", getDescription().getVersion());

			try {
				players.save();
			}catch (Exception ex) {
				logger.severe("Error when saving player datas.", ex);
			}
			data.save(dataFile);
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Saved datas (" + (((double) System.currentTimeMillis() - time) / 1000D) + "s)!");
		}

		if (unload){
			npcManager.unload();
			resetDatas();
		}
	}

	private void resetDatas(){
		quests = null;
		pools = null;
		try {
			if (db != null) db.close();
		}catch (Exception ex) {
			logger.severe("An error occurred while closing database connection.", ex);
		}
		players = null;
		//HandlerList.unregisterAll(this);
		loaded = false;
	}

	/* ---------- Backups ---------- */

	public boolean createFolderBackup(Path backup) {
		if (!config.backups)
			return false;
		logger.info("Creating quests backup...");
		Path backupDir = backup.resolve("quests");
		Path saveFolderPath = saveFolder.toPath();
		try (Stream<Path> stream = Files.walk(saveFolderPath)) {
			Files.createDirectories(backupDir);
			stream.forEach(path -> {
				if (path.equals(saveFolderPath)) return;
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

	public boolean createDataBackup(Path backup) {
		if (!config.backups)
			return false;
		logger.info("Creating data backup...");
		try{
			Path target = backup.resolve("data.yml");
			if (Files.exists(target)) {
				logger.warning("File " + target.toString() + " already exist. This should not happen.");
			}else {
				Files.createDirectories(backup);
				logger.info("Datas backup created in " + Files.copy(dataFile.toPath(), target).getParent().getFileName());
			}
			return true;
		}catch (Exception e) {
			logger.severe("An error occured while creating the backup.", e);
			return false;
		}
	}

	public boolean createPlayerDatasBackup(Path backup, PlayersManagerYAML yamlManager) {
		if (!config.backups)
			return false;

		logger.info("Creating player datas backup...");
		Path backupDir = backup.resolve("players");
		Path playersFolderPath = yamlManager.getDirectory().toPath();
		try (Stream<Path> stream = Files.walk(playersFolderPath)) {
			Files.createDirectories(backupDir);
			stream.forEach(path -> {
				if (path.equals(playersFolderPath)) return;
				try {
					Files.copy(path, backupDir.resolve(playersFolderPath.relativize(path)));
				}catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
			logger.info("Player datas backup created in " + backupDir.getFileName().toString());
			return true;
		}catch (Exception e) {
			logger.severe("An error occured while creating the backup.", e);
			return false;
		}
	}

	public boolean createQuestBackup(Path file, String msg) {
		if (!config.backups)
			return false;
		logger.info("Creating single quest backup...");
		try{
			Path target = Paths.get(file.toString() + "-backup" + format.format(new Date()) + ".yml");
			if (Files.exists(target)) {
				logger.warning("File " + target.toString() + " already exist. This should not happen.");
			}else {
				logger.info("Quest backup created at " + Files.copy(file, target).getFileName());
			}
			return true;
		}catch (Exception e) {
			logger.severe("An error occured while creating the backup.", e);
			return false;
		}
	}

	private SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'-'hh'-'mm'-'ss");

	public Path backupDir() {
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
					sender.sendMessage("§a " + quests.getQuests().size() + " quests loaded");
					sender.sendMessage("§a§lPlugin entierely reloaded from files !");
				} catch (Throwable e) {
					sender.sendMessage("§cError when loading the data file. §lOperation failed!");
					e.printStackTrace();
				}
			}
		}.runTaskLater(BeautyQuests.getInstance(), 20L);
	}

	@Override
	public void notifyLoadingFailure() {
		loadingFailure = true;
	}

	public void resetLoadingFailure() {
		loadingFailure = false;
	}

	public boolean hasLoadingFailed() {
		return loadingFailure;
	}

	@Override
	public void notifySavingFailure() {
		savingFailure = true;
	}

	public void resetSavingFailure() {
		savingFailure = false;
	}

	public boolean hasSavingFailed() {
		return savingFailure;
	}

	private <T> @NotNull T ensureLoaded(@Nullable T object) {
		if (object == null)
			throw new IllegalStateException("BeautyQuests is not yet initialized");
		return object;
	}

	@Override
	public @NotNull LoggerExpanded getLoggerExpanded() {
		return ensureLoaded(logger);
	}

	public @NotNull String getPrefix() {
		return config.getPrefix();
	}

	@Override
	public @NotNull CommandsManagerImplementation getCommand() {
		return ensureLoaded(command);
	}

	@Override
	public @NotNull QuestsConfigurationImplementation getConfiguration() {
		return config;
	}

	public @NotNull FileConfiguration getDataFile() {
		return data;
	}

	public @Nullable Database getBQDatabase() {
		return db;
	}

	public @Nullable ScoreboardManager getScoreboardManager() {
		return scoreboards;
	}

	public @NotNull QuestsManagerImplementation getQuestsManager() {
		return ensureLoaded(quests);
	}

	public @NotNull QuestPoolsManagerImplementation getPoolsManager() {
		return ensureLoaded(pools);
	}

	@Override
	public @NotNull GuiManager getGuiManager() {
		return ensureLoaded(guiManager);
	}

	@Override
	public @NotNull EditorManager getEditorManager() {
		return ensureLoaded(editorManager);
	}

	@Override
	public @NotNull BqNpcManagerImplementation getNpcManager() {
		return npcManager;
	}

	@Override
	public @NotNull IntegrationManager getIntegrationManager() {
		return integrations;
	}

	@Override
	public @NotNull QuestsAPIImplementation getAPI() {
		return QuestsAPIImplementation.INSTANCE;
	}

	@Override
	public @NotNull AbstractPlayersManager getPlayersManager() {
		return ensureLoaded(players);
	}

	public boolean isRunningPaper() {
		return isPaper;
	}


	public static @NotNull BeautyQuests getInstance() {
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
