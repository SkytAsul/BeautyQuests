package fr.skytasul.quests;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import com.tchristofferson.configupdater.ConfigUpdater;
import fr.skytasul.quests.api.QuestsHandler;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.editors.EditorManager;
import fr.skytasul.quests.api.events.internal.BeautyQuestsLoadedEvent;
import fr.skytasul.quests.api.gui.GuiManager;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.localization.Locale;
import fr.skytasul.quests.api.questers.data.QuesterDataManager;
import fr.skytasul.quests.api.utils.IntegrationManager;
import fr.skytasul.quests.api.utils.MinecraftNames;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.Version;
import fr.skytasul.quests.api.utils.logger.LoggerExpanded;
import fr.skytasul.quests.commands.CommandsManagerImplementation;
import fr.skytasul.quests.editor.ChatEditorFactory;
import fr.skytasul.quests.editor.DialogEditorFactory;
import fr.skytasul.quests.editor.EditorManagerImplementation;
import fr.skytasul.quests.gui.GuiManagerImplementation;
import fr.skytasul.quests.npcs.BqNpcManagerImplementation;
import fr.skytasul.quests.players.PlayerManagerImplementation;
import fr.skytasul.quests.players.accounts.PlayerManagerAccountsHookImplementation;
import fr.skytasul.quests.questers.QuesterManagerImplementation;
import fr.skytasul.quests.questers.data.sql.SqlDataManager;
import fr.skytasul.quests.questers.data.yaml.YamlDataManager;
import fr.skytasul.quests.scoreboards.ScoreboardManager;
import fr.skytasul.quests.structure.QuestsManagerImplementation;
import fr.skytasul.quests.structure.pools.QuestPoolsManagerImplementation;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.compatibility.InternalIntegrations;
import fr.skytasul.quests.utils.logger.BqLoggerHandler;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.nms.NullNMS;
import io.papermc.paper.ServerBuildInfo;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
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

public abstract class BeautyQuests extends JavaPlugin implements QuestsPlugin {

	private static BeautyQuests instance;

	private Version serverVersion;
	private BukkitRunnable saveTask;
	private NMS nms;
	private QuestsAPIImplementation api;

	/* --------- Storage --------- */

	private String lastVersion;
	private boolean dontUpdateLastVersion = false;
	private QuestsConfigurationImplementation config;

	private String loadedLanguage;

	private Database db;

	private YamlConfiguration data;
	private File dataFile;
	private File saveFolder;

	private boolean doneBackup = false;
	private final boolean unitTesting;

	/* --------- Datas --------- */

	private final @NotNull BqNpcManagerImplementation npcManager = new BqNpcManagerImplementation();
	private @Nullable ScoreboardManager scoreboards;
	private @Nullable QuestsManagerImplementation quests;
	private @Nullable QuestPoolsManagerImplementation pools;
	private @Nullable QuesterManagerImplementation questerManager;
	private @Nullable PlayerManagerImplementation players;

	/* ---------- Operations -------- */

	private boolean disable = false;
	private boolean loadingFailure = false;
	protected boolean loaded = false;

	private @NotNull IntegrationManager integrations = new IntegrationManager();
	private @Nullable CommandsManagerImplementation command;
	protected @Nullable LoggerExpanded logger;
	private @Nullable BqLoggerHandler loggerHandler;
	private @Nullable GuiManagerImplementation guiManager;
	private @Nullable EditorManagerImplementation editorManager;

	/* ---------------------------------------------- */

	public BeautyQuests(Boolean unitTesting) {
		this.unitTesting = unitTesting;
	}

	private void fatalError() {
		logger.severe("This is a fatal error. Now disabling.");
		disable = true;
		setEnabled(false);
	}

	@Override
	public void onLoad(){
		instance = this;

		initLogger();

		try {
			initApi();
		} catch (Exception ex) {
			logger.severe("An unexpected exception occurred while initializing the API.", ex);
			fatalError();
		}
	}

	@Override
	public void onEnable(){
		if (disable)
			return;

		try {
			logger.info("------------ BeautyQuests ------------");

			logDebugInformation();

			saveDefaultConfig();
			initInternals();

			saveFolder = new File(getDataFolder(), "quests");
			if (!saveFolder.exists()) saveFolder.mkdirs();
			loadDataFile();
			checkLastVersion();
			loadDefaultIntegrations(); // used later in full initialization
			loadConfigParameters(true);

			if (!unitTesting)
				registerCommands();

			try {
				Bukkit.getPluginManager().registerEvents(integrations, this);

				integrations.initializeCompatibilities();
			}catch (Exception ex) {
				logger.severe("An error occurred while initializing compatibilities. Consider restarting.", ex);
			}

			// Launch loading task
			Bukkit.getScheduler().runTask(this, () -> {
				// At this point, we know how much time we need to wait before loading the quests
				long timeToWait = npcManager.getTimeToWaitForNPCs();
				if (timeToWait > 0) {
					Bukkit.getScheduler().runTaskLater(this, this::finishLoad, timeToWait);
				} else {
					finishLoad();
				}
			});

			// Start of non-essential systems
			if (!unitTesting) {
				String pluginVersion = getDescription().getVersion();
				launchMetrics(pluginVersion);
				try {
					launchUpdateChecker(pluginVersion);
				} catch (Exception e) {
					logger.severe("An error occurred while checking updates.", e);
				}
			}
		}catch (LoadingException ex) {
			if (ex.getCause() != null) logger.severe("A fatal error occurred while loading plugin.", ex.getCause());
			logger.severe(ex.loggerMessage);
			fatalError();
		}catch (Exception ex) {
			logger.severe("An unexpected exception occurred while loading plugin.", ex);
			fatalError();
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

			try {
				QuestsAPIProvider.removeAPI();
			} catch (Exception ex) {
				logger.severe("Failed to disable the API", ex);
			}

			getServer().getScheduler().cancelTasks(this);
		}finally {
			if (loggerHandler != null)
				loggerHandler.close();
		}
	}

	/* ---------- Various init ---------- */

	private void initLogger() {
		loggerHandler = null;
		if (!isUnitTesting()) {
			try {
				Files.createDirectories(getDataFolder().toPath());
				loggerHandler = new BqLoggerHandler(this);
				getLogger().addHandler(loggerHandler);
				getLogger().setLevel(LoggerExpanded.DEBUG_LEVEL);
			} catch (Throwable ex) {
				getLogger().log(Level.SEVERE, "Failed to insert logging handler.", ex);
			}
		}

		logger = new LoggerExpanded(getLogger());
	}

	private void logDebugInformation() {
		logger.debug("Java runtime: {0} {1}", System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"));
		logger.debug("System: {0} {1}", System.getProperty("os.name"), System.getProperty("os.version"));
		logger.debug("System locale: {0}", java.util.Locale.getDefault());
	}

	private void initInternals() {
		String minecraftVersion = ServerBuildInfo.buildInfo().minecraftVersionId();
		logger.debug("Minecraft version from build info: {0}", minecraftVersion);
		serverVersion = Version.parse(minecraftVersion);

		if (unitTesting) {
			nms = new NullNMS();
			return;
		}

		QuestsPlugin.getPlugin().getLoggerExpanded().debug("Detected server version: {0}. Bukkit version: {1}",
				getServerVersion(), Bukkit.getVersion());
		try {
			nms = createInternalsAccess();
			logger.info("Full integration with version {0}!", getServerVersion());
		} catch (Exception ex) {
			logger.severe("Unexpected exception during internals creation", ex);
		}

		if (nms == null) {
			nms = new NullNMS();
			logger.warning("Some functionnalities of the plugin have not been enabled.");
		}
	}

	private void initApi() throws ReflectiveOperationException {
		api = new QuestsAPIImplementation(this);
		QuestsAPIProvider.initializeAPI(api);
	}

	private void registerCommands(){
		command = new CommandsManagerImplementation(this);
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
			String[] split = version.split("\\+");
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
		metrics.addCustomChart(new SimplePie("scoreboards", () -> scoreboards == null ? "Disabled" : "Enabled"));
		logger.debug("Started bStats metrics");
	}

	private void launchUpdateChecker(String pluginVersion) {
		logger.debug("Starting Spigot updater");
		UpdateChecker checker;
		if (pluginVersion.contains("_")) {
			Matcher matcher = Pattern.compile("\\+build\\.(\\d+)").matcher(pluginVersion);
			if (matcher.find()) {
				String build = matcher.group(1);
				checker = new UpdateChecker(this, UpdateCheckSource.GITHUB_RELEASE_TAG, "SkytAsul/BeautyQuests")
						.setUserAgent("")
						.setDownloadLink("https://ci.codemc.io/job/SkytAsul/job/BeautyQuests")
						.setUsedVersion("build/" + build)
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
				.setDonationLink("https://ko-fi.com/skytasul")
				.setSupportLink("https://discord.gg/H8fXrkD")
				.setNotifyOpsOnJoin(false)
				.setColoredConsoleOutput(true)
				.checkNow();
	}

	/* ---------- YAML ---------- */

	private void loadConfigParameters(boolean init) throws LoadingException {
		try{
			File configFile = new File(getDataFolder(), "config.yml");
			config = new QuestsConfigurationImplementation(getConfig(), data);
			if (config.update()) {
				config.getConfig().save(configFile);
				logger.info("Updated config.");
			}
			if (init) loadLang();
			ConfigUpdater.update(this, "config.yml", configFile);
			config.init();

			initTranslations();

			if (config.getDatabaseConfig().enabled()) {
				db = null;
				try {
					db = new Database(config.getDatabaseConfig());
					db.testConnection();
					logger.info("Connection to database etablished.");
				}catch (Exception ex) {
					db = null;
					throw new LoadingException("Connection to database has failed.", ex);
				}
			}

			QuesterDataManager questerDataManager;
			if (db == null) {
				var dataManager = new YamlDataManager(getDataFolder().toPath().resolve("questers"));
				Path oldDataPath = getDataFolder().toPath().resolve("players");
				if (Files.exists(oldDataPath)) {
					// TODO remove migration 2.0
					if (dataManager.migrate(oldDataPath, data))
						data.save(dataFile);
				}
				questerDataManager = dataManager;
			} else {
				questerDataManager = new SqlDataManager(db);
			}
			questerManager = new QuesterManagerImplementation(this, questerDataManager);
			if (config.hookAccounts()) {
				QuestsPlugin.getPlugin().getLoggerExpanded().info("AccountsHook is now managing quester datas!");
				players = new PlayerManagerAccountsHookImplementation(this);
			} else
				players = new PlayerManagerImplementation(this);
			questerManager.registerQuesterProvider(players);

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

				var editorFactory = serverVersion.isAfter(1, 21, 8) ? new DialogEditorFactory() : new ChatEditorFactory();
				getServer().getPluginManager().registerEvents(editorManager = new EditorManagerImplementation(editorFactory), this);
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
		integrations.testCompatibilities();
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
		if (dataFile.exists()) {
			logger.debug("Loading data file, last time edited: {}", new Date(dataFile.lastModified()).toString());
			data = YamlConfiguration.loadConfiguration(dataFile);
		} else {
			try {
				dataFile.createNewFile();
				data = new YamlConfiguration();
				logger.debug("Created data file.");
			} catch (IOException e) {
				throw new LoadingException("Couldn't create data file.", e);
			}
		}
		data.options().header("Internal use only. Do not edit ANYTHING here.");
		data.options().copyHeader(true);
	}

	private void checkLastVersion() throws LoadingException {
		if (data.contains("version")){
			lastVersion = data.getString("version");
			if (!lastVersion.equals(getDescription().getVersion())){
				logger.info("You are using a new version for the first time. (last version: " + lastVersion + ")");

				var matcher = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)").matcher(lastVersion);
				if (matcher.find()) {
					int major = Integer.parseInt(matcher.group(1));
					int minor = Integer.parseInt(matcher.group(2));
					// int patch = Integer.parseInt(matcher.group(3));
					if (major == 0 && minor < 20) {
						dontUpdateLastVersion = true;
						throw new LoadingException(
								"Data migration between %s and %s cannot happen. Please start from a fresh BeautyQuests install."
										.formatted(lastVersion, getDescription().getVersion()));
					}
				} else {
					logger.warning("Cannot parse last version to ensure data migration is possible.");
				}

				try {
					performBackup();
					doneBackup = true;
				} catch (IOException ex) {
					logger.warning("Failed to create a backup", ex);
				}
			}
		}else lastVersion = getDescription().getVersion();
	}

	private void initTranslations() {
		String fileName = config.getMinecraftTranslationsFile();
		if (fileName == null || fileName.isBlank())
			return;

		Optional<String> extension = Utils.getFilenameExtension(fileName);
		if (extension.isPresent()) {
			if (!extension.get().equalsIgnoreCase("json")) {
				logger.warning("File {} is not a JSON file.", fileName);
				return;
			}
		} else {
			fileName += ".json";
		}

		try {
			MinecraftNames.intialize(QuestsPlugin.getPlugin().getDataFolder().toPath().resolve(fileName));
		} catch (Exception ex) {
			logger.severe("An error occurred when loading Minecraft Vanilla Translations from {}.", ex, fileName);
		}
	}

	private void finishLoad() {
		if (disable)
			return;

		try {
			integrations.lockDependencies();

			initScoreboards();
			loadQuestData();
			questerManager.load();
			loadQuestHandlers();

			Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
				players.loadOnlinePlayers();
				loaded = true;

				Bukkit.getPluginManager().callEvent(new BeautyQuestsLoadedEvent());
			}, 0L);

			getServer().getPluginManager().registerEvents(new QuestsListener(), BeautyQuests.this);

			if (!unitTesting)
				launchSaveCycle();

			String pluginVersion = getDescription().getVersion();
			if (!lastVersion.equals(pluginVersion)) { // maybe change in data structure : update of all quest files
				logger.debug("Migrating from " + lastVersion + " to " + pluginVersion);
				int updated = quests.updateAll();
				if (updated > 0)
					logger.info("Updated " + updated + " quests during migration.");
				pools.updateAll();
				saveAllConfig(false);
			}
		} catch (Throwable e) {
			logger.severe("An error occurred while loading plugin datas.", e);
			fatalError();
		}
	}

	private void loadQuestData() throws IOException {
		long lastMillis = System.currentTimeMillis();
		pools = new QuestPoolsManagerImplementation(this, new File(getDataFolder(), "questPools.yml"));
		quests = new QuestsManagerImplementation(this, data.getInt("lastID"), saveFolder);
		logger.info("{0} quests and {1} pools loaded ({2}s)!", quests.getQuestsAmount(), pools.getPools().size(),
				((double) System.currentTimeMillis() - lastMillis) / 1000D);
	}

	private void loadQuestHandlers() {
		for (var iterator = getAPI().getQuestsHandlers().iterator(); iterator.hasNext();) {
			QuestsHandler handler = iterator.next();
			try {
				handler.load();
			}catch (Exception ex) {
				logger.severe("Cannot load quest handler " + handler.getClass().getName(), ex);
				iterator.remove();
			}
		}
	}

	private void initScoreboards() throws IOException {
		if (scoreboards == null && config.getQuestsConfig().scoreboards()) {
			File scFile = new File(getDataFolder(), "scoreboard.yml");
			if (!scFile.exists())
				saveResource("scoreboard.yml", false);
			ConfigUpdater.update(this, "scoreboard.yml", scFile);
			scoreboards = new ScoreboardManager(this, scFile);
			getAPI().registerQuestsHandler(scoreboards);
		}
	}

	public void saveAllConfig(boolean unload) throws Exception {
		if (unload) {
			if (quests != null)
				quests.unloadQuests();
			if (pools != null)
				pools.unloadAll();

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
			if (!dontUpdateLastVersion)
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

		logger.info("Performed backup at {0}.", backupDir);
	}

	public boolean performDataReload() {
		quests.unloadQuests();
		pools.unloadAll();

		try {
			loadQuestData();
			return true;
		} catch (IOException ex) {
			logger.severe("An error occured while reloading the plugin data.", ex);
			fatalError();
			return false;
		}
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

	private <T> @NotNull T ensureLoaded(@Nullable T object) {
		if (object == null)
			throw new IllegalStateException("BeautyQuests is not yet initialized");
		return object;
	}

	@Override
	public @NotNull LoggerExpanded getLoggerExpanded() {
		return ensureLoaded(logger);
	}

	public @Nullable BqLoggerHandler getLoggerHandler() {
		return loggerHandler;
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
		return api;
	}

	@Override
	public @NotNull PlayerManagerImplementation getPlayersManager() {
		return ensureLoaded(players);
	}

	public @NotNull QuesterManagerImplementation getQuesterManager() {
		return ensureLoaded(questerManager);
	}

	public @NotNull NMS getInternalsAccess() {
		return nms;
	}

	@Override
	public @NotNull Version getServerVersion() {
		return ensureLoaded(serverVersion);
	}

	public boolean isCompletelyLoaded() {
		return loaded;
	}

	public boolean isUnitTesting() {
		return unitTesting;
	}

	protected abstract @Nullable NMS createInternalsAccess();


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
