package fr.skytasul.quests;

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
import fr.skytasul.quests.players.PlayerManagerImplementation;
import fr.skytasul.quests.players.accounts.PlayerManagerAccountsHookImplementation;
import fr.skytasul.quests.questers.QuesterManagerImplementation;
import fr.skytasul.quests.questers.data.QuesterDataManager;
import fr.skytasul.quests.questers.data.sql.SqlDataManager;
import fr.skytasul.quests.questers.data.yaml.YamlDataManager;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.structure.QuestImplementation;
import fr.skytasul.quests.structure.QuestsManagerImplementation;
import fr.skytasul.quests.structure.pools.QuestPoolsManagerImplementation;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.compatibility.InternalIntegrations;
import fr.skytasul.quests.utils.compatibility.Paper;
import fr.skytasul.quests.utils.compatibility.Post1_16;
import fr.skytasul.quests.utils.logger.LoggerHandler;
import fr.skytasul.quests.utils.nms.NMS;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BeautyQuests extends JavaPlugin implements QuestsPlugin {

	private static BeautyQuests instance;
	private BukkitRunnable saveTask;
	private @Nullable Paper paperCompat;

	/* --------- Storage --------- */

	private String lastVersion;
	private QuestsConfigurationImplementation config;

	private String loadedLanguage;

	private Database db;

	private YamlConfiguration data;
	private File dataFile;
	private File saveFolder;

	private boolean doneBackup = false;

	/* --------- Datas --------- */

	private final @NotNull BqNpcManagerImplementation npcManager = new BqNpcManagerImplementation();
	private @Nullable ScoreboardManager scoreboards;
	private @Nullable QuestsManagerImplementation quests;
	private @Nullable QuestPoolsManagerImplementation pools;
	private @Nullable QuesterManagerImplementation questerManager;
	private @Nullable PlayerManagerImplementation players;

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
	private @Nullable BukkitAudiences audiences;

	/* ---------------------------------------------- */

	@Override
	public void onLoad(){
		instance = this;

		checkPaper();
		initLibraries();

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

			if (isRunningPaper())
				logger.debug("Paper detected.");
			else
				logger.warning("You are not running the Paper software.\n"
						+ "It is highly recommended to use it for extended features and more stability.");

			audiences = BukkitAudiences.create(this);

			saveDefaultConfig();
			NMS.isValid(); // to force initialization

			saveFolder = new File(getDataFolder(), "quests");
			if (!saveFolder.exists()) saveFolder.mkdirs();
			loadDataFile();
			checkLastVersion();
			loadConfigParameters(true);

			registerCommands();

			try {
				loadDefaultIntegrations();
				integrations.testCompatibilities();
				Bukkit.getPluginManager().registerEvents(integrations, this);

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
							logger.debug("Migrating from " + lastVersion + " to " + pluginVersion);
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

	private void checkPaper() {
		try {
			if (Class.forName("com.destroystokyo.paper.ParticleBuilder") != null) {
				paperCompat = (Paper) Class.forName("fr.skytasul.quests.utils.compatibility.PaperImplementation")
						.getDeclaredConstructor().newInstance();
			}
		} catch (Exception ex) {
			paperCompat = null;
		}
	}

	private void initLibraries() {
		/*var libManager = isPaper ? new PaperLibraryManager(this) : new BukkitLibraryManager(this);

		libManager.addMavenCentral();
		libManager.addMavenLocal();

		libManager.loadLibrary(Library.builder()
				.groupId("com{}github{}Revxrsal{}Lamp")
				.artifactId("bukkit")
				.version("3.1.1")
				.relocate("revxrsal{}commands", "fr{}skytasul{}quests{}api{}commands{}revxrsal")
				.build());
		libManager.loadLibrary(Library.builder()
				.groupId("com{}github{}Revxrsal{}Lamp")
				.artifactId("bukkit")
				.version("3.1.1")
				.relocate("revxrsal{}commands", "fr{}skytasul{}quests{}api{}commands{}revxrsal")
				.build());
		libManager.loadLibrary(Library.builder()
				.groupId("com{}github{}cryptomorin")
				.artifactId("XSeries")
				.version("11.2.0")
				.relocate("com{}cryptomorin{}xseries", "fr{}skytasul{}quests{}api{}utils")
				.build());*/
	}

	private void initApi() throws ReflectiveOperationException {
		Method setMethod = QuestsAPIProvider.class.getDeclaredMethod("setAPI", QuestsAPI.class);
		setMethod.setAccessible(true); // NOSONAR
		setMethod.invoke(null, QuestsAPIImplementation.INSTANCE);
	}

	private void registerCommands(){
		command = new CommandsManagerImplementation();
		command.initializeCommands();
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
		metrics.addCustomChart(new DrilldownPie("hooks_v2", () -> {
			return integrations.getDependencies()
					.stream()
					.filter(dep -> dep.isEnabled())
					.map(dep -> dep.getFoundPlugin())
					.distinct()
					.collect(Collectors.toMap(Plugin::getName, plugin -> {
						Map<String, Integer> entry = new HashMap<>();
						entry.put(plugin.getDescription().getVersion(), 1);
						return entry;
					}));
		}));
		logger.debug("Started bStats metrics");
	}

	private void launchUpdateChecker(String pluginVersion) {
		logger.debug("Starting Spigot updater");
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
				.setColoredConsoleOutput(true)
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

			QuesterDataManager questerDataManager = db == null ? new YamlDataManager() : new SqlDataManager(db);
			questerManager = new QuesterManagerImplementation(questerDataManager);
			if (config.hookAccounts()) {
				QuestsPlugin.getPlugin().getLoggerExpanded().info("AccountsHook is now managing quester datas!");
				players = new PlayerManagerAccountsHookImplementation(questerManager);
			} else
				players = new PlayerManagerImplementation(questerManager);

			/*				static initialization				*/
			if (init) {
				getAPI().setup();
				logger.debug("Initializing default stage types.");
				DefaultQuestFeatures.registerStages();
				logger.debug("Initializing default quest options.");
				DefaultQuestFeatures.registerQuestOptions();
				logger.debug("Initializing default item comparisons.");
				DefaultQuestFeatures.registerItemComparisons();
				logger.debug("Initializing default rewards.");
				DefaultQuestFeatures.registerRewards();
				logger.debug("Initializing default requirements.");
				DefaultQuestFeatures.registerRequirements();
				logger.debug("Initializing default stage options.");
				DefaultQuestFeatures.registerStageOptions();
				logger.debug("Initializing default miscellenaeous.");
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

	private void loadLang() throws LoadingException {
		try {
			loadedLanguage = config.getConfig().getString("lang", "en_US");
			Locale.loadLang(this, Lang.values(), loadedLanguage);

			Pattern oldPlaceholders = Pattern.compile("\\{\\d\\}");
			for (Lang l : Lang.values()) {
				if (oldPlaceholders.matcher(l.getValue()).find()) {
					logger.warning(
							"Found old placeholder format in /plugins/BeautyQuests/locales/" + loadedLanguage + ".yml.");
					logger.warning(
							"This means you probably have not deleted the locales folder after upgrading from a pre-1.0 version."
									+ " Expect some bugs with message formatting.");
				}
			}
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
		logger.debug("Loading data file, last time edited : " + new Date(dataFile.lastModified()).toString());
		data = YamlConfiguration.loadConfiguration(dataFile);
		data.options().header("Do not edit ANYTHING here.");
		data.options().copyHeader(true);
	}

	private void checkLastVersion() {
		if (data.contains("version")){
			lastVersion = data.getString("version");
			if (!lastVersion.equals(getDescription().getVersion())){
				logger.info("You are using a new version for the first time. (last version: " + lastVersion + ")");

				// TODO manage incompatible upgrade (e.g. pre-1.0)

				try {
					performBackup();
					doneBackup = true;
				} catch (IOException ex) {
					logger.warning("Failed to create a backup", ex);
				}
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
			questerManager.load();
			players.loadOnlinePlayers();
		}catch (Exception ex) {
			logger.severe("Error while loading player datas.", ex);

			if (!doneBackup) {
				try {
					performBackup();
					doneBackup = true;
				} catch (IOException exBackup) {
					logger.warning("Failed to create a backup.", exBackup);
				}
			}
		}

		pools = new QuestPoolsManagerImplementation(new File(getDataFolder(), "questPools.yml"));
		quests = new QuestsManagerImplementation(this, data.getInt("lastID"), saveFolder);

		getAPI().getQuestsHandlers().forEach(handler -> {
			try {
				handler.load();
			}catch (Exception ex) {
				logger.severe("Cannot load quest handler " + handler.getClass().getName(), ex);
			}
		});

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
			players.loadOnlinePlayers();
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
				questerManager.saveAll();
			}catch (Exception ex) {
				logger.severe("Error when saving player datas.", ex);
			}
			data.save(dataFile);
			logger.debug("Saved datas (" + (((double) System.currentTimeMillis() - time) / 1000D) + "s)!");
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

	public void performBackup() throws IOException {
		Path dataDir = getDataFolder().toPath();
		Path backupDir = dataDir.resolve("backup-" + format.format(new Date()));
		Files.createDirectory(backupDir);

		var files = Files.list(dataDir)
				.filter(path -> !path.getFileName().toString().startsWith("backup-"))
				.filter(path -> !path.getFileName().toString().equals("locales"))
				.filter(path -> !path.getFileName().toString().endsWith(".log"))
				.toList();

		for (Path file : files) {
			Files.walk(file).forEach(source -> {
				Path destination = backupDir.resolve(dataDir.relativize(source));
				try {
					Files.copy(source, destination);
				} catch (IOException ex) {
					throw new UncheckedIOException(ex);
				}
			});
		}

		logger.info("Performed backup at {}.", backupDir);
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
	public @NotNull PlayerManagerImplementation getPlayersManager() {
		return ensureLoaded(players);
	}

	public @NotNull QuesterManagerImplementation getQuesterManager() {
		return ensureLoaded(questerManager);
	}

	@Override
	public @NotNull BukkitAudiences getAudiences() {
		return ensureLoaded(audiences);
	}

	public boolean isRunningPaper() {
		return paperCompat != null;
	}

	public Optional<Paper> getPaperCompatibility() {
		return Optional.ofNullable(paperCompat);
	}

	public boolean isCompletelyLoaded() {
		return loaded;
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
