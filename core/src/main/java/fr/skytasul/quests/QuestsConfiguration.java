package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.structure.QuestDescription;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.OrdinaryColor;
import fr.skytasul.quests.utils.ParticleEffect.Particle;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Accounts;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.nms.NMS;

public class QuestsConfiguration {

	private static int timer = 5;
	private static String lang;
	private static String minecraftTranslationsFile = null;
	private static int maxLaunchedQuests = 0;
	private static boolean sounds = true;
	private static boolean fireworks = true;
	private static boolean gps = false;
	private static boolean skillAPIoverride = true;
	private static boolean scoreboard = true;
	private static String finishSound = "ENTITY_PLAYER_LEVELUP";
	private static ItemStack item = XMaterial.BOOK.parseItem();
	private static XMaterial pageItem = XMaterial.ARROW;
	private static int startParticleDistance, startParticleDistanceSquared;
	private static int requirementUpdateTime;
	private static boolean enablePrefix = true;
	private static double hologramsHeight = 0.0;
	private static boolean disableTextHologram = false;
	private static boolean showCustomHologramName = true;
	private static boolean mobsProgressBar = false;
	private static int progressBarTimeoutSeconds = 15;
	private static boolean hookAcounts = false;
	private static ParticleEffect.Particle particleStart;
	private static ParticleEffect.Particle particleTalk;
	private static ParticleEffect.Particle particleNext;
	private static boolean sendUpdate = true;
	private static boolean stageStart = true;
	private static boolean playerCancelQuest = false;
	private static boolean questConfirmGUI = false;
	private static ClickType npcClick = ClickType.RIGHT;
	private static String dSetName = "Quests";
	private static String dIcon = "bookshelf";
	private static int dMinZoom = 0;
	// stageDescription
	private static String itemNameColor;
	private static String itemAmountColor;
	private static String stageDescriptionFormat = "§8({0}/{1}) §e{2}";
	private static String descPrefix = "{nl}§e- §6";
	private static String descAmountFormat = "x{0}";
	private static boolean descXOne = true;
	private static boolean inlineAlone = true;
	private static List<Source> descSources = new ArrayList<>();
	private static boolean requirementReasonOnMultipleQuests = true;
	private static boolean menuOpenNotStartedTabWhenEmpty = true;
	private static boolean stageEndRewardsMessage = true;
	private static QuestDescription questDescription;
	
	private static ItemStack holoLaunchItem = null;
	private static ItemStack holoLaunchNoItem = null;
	private static ItemStack holoTalkItem = null;

	static boolean backups = true;
	
	static boolean saveCycleMessage = true;
	static int saveCycle = 15;
	static int firstQuestID = -1; // changed in 0.19, TODO
	
	private FileConfiguration config;
	private DialogsConfig dialogs;
	
	QuestsConfiguration(BeautyQuests plugin) {
		config = plugin.getConfig();
		dialogs = new DialogsConfig(config.getConfigurationSection("dialogs"));
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public DialogsConfig getDialogs() {
		return dialogs;
	}
	
	boolean update() {
		boolean result = false;
		result |= dialogs.update();
		return result;
	}
	
	void init() {
		backups = config.getBoolean("backups", true);
		if (!backups) BeautyQuests.logger.warning("Backups are disabled due to the presence of \"backups: false\" in config.yml.");
		
		timer = config.getInt("redoMinuts");
		lang = config.getString("lang", "en_US");
		minecraftTranslationsFile = config.getString("minecraftTranslationsFile");
		if (isMinecraftTranslationsEnabled()) {
			if (NMS.getMCVersion() >= 13) {
				if (!MinecraftNames.intialize(minecraftTranslationsFile)) {
					BeautyQuests.logger.warning("Cannot enable the \"minecraftTranslationsFile\" option : problem when initializing");
					minecraftTranslationsFile = null;
				}
			}else{
				BeautyQuests.logger.warning("Cannot enable the \"minecraftTranslationsFile\" option : only supported on Spigot 1.13 and higher");
				minecraftTranslationsFile = null;
			}
		}
		dialogs.init();
		
		saveCycle = config.getInt("saveCycle");
		saveCycleMessage = config.getBoolean("saveCycleMessage");
		firstQuestID = config.getInt("firstQuest", -1);
		maxLaunchedQuests = config.getInt("maxLaunchedQuests");
		sendUpdate = config.getBoolean("playerQuestUpdateMessage");
		stageStart = config.getBoolean("playerStageStartMessage");
		playerCancelQuest = config.getBoolean("allowPlayerCancelQuest");
		questConfirmGUI = config.getBoolean("questConfirmGUI");
		sounds = config.getBoolean("sounds");
		fireworks = config.getBoolean("fireworks");
		gps = DependenciesManager.gps.isEnabled() && config.getBoolean("gps");
		skillAPIoverride = config.getBoolean("skillAPIoverride");
		scoreboard = config.getBoolean("scoreboards");
		if (config.isItemStack("item")) {
			item = config.getItemStack("item");
		}else if (config.isString("item")) {
			item = XMaterial.matchXMaterial(config.getString("item")).orElse(XMaterial.BOOK).parseItem();
		}else item = XMaterial.BOOK.parseItem();
		if (config.contains("pageItem")) pageItem = XMaterial.matchXMaterial(config.getString("pageItem")).orElse(XMaterial.ARROW);
		if (pageItem == null) pageItem = XMaterial.ARROW;
		startParticleDistance = config.getInt("startParticleDistance");
		startParticleDistanceSquared = startParticleDistance * startParticleDistance;
		requirementUpdateTime = config.getInt("requirementUpdateTime");
		requirementReasonOnMultipleQuests = config.getBoolean("requirementReasonOnMultipleQuests");
		menuOpenNotStartedTabWhenEmpty = config.getBoolean("menuOpenNotStartedTabWhenEmpty");
		stageEndRewardsMessage = config.getBoolean("stageEndRewardsMessage");
		mobsProgressBar = config.getBoolean("mobsProgressBar");
		progressBarTimeoutSeconds = config.getInt("progressBarTimeoutSeconds");
		try {
			npcClick = ClickType.valueOf(config.getString("npcClick").toUpperCase());
		}catch (IllegalArgumentException ex) {
			BeautyQuests.logger.warning("Unknown click type " + config.getString("npcClick") + " for config entry \"npcClick\"");
		}
		enablePrefix = config.getBoolean("enablePrefix");
		disableTextHologram = config.getBoolean("disableTextHologram");
		showCustomHologramName = config.getBoolean("showCustomHologramName");
		hologramsHeight = 0.28 + config.getDouble("hologramsHeight");
		hookAcounts = DependenciesManager.acc.isEnabled() && config.getBoolean("accountsHook");
		if (hookAcounts) {
			Bukkit.getPluginManager().registerEvents(new Accounts(), BeautyQuests.getInstance());
			BeautyQuests.logger.info("AccountsHook is now managing player datas for quests !");
		}
		dSetName = config.getString("dynmap.markerSetName");
		if (dSetName == null || dSetName.isEmpty()) DependenciesManager.dyn.disable();
		dIcon = config.getString("dynmap.markerIcon");
		dMinZoom = config.getInt("dynmap.minZoom");
		finishSound = config.getString("finishSound");
		try{
			Sound.valueOf(finishSound.toUpperCase());
		}catch (IllegalArgumentException ex){
			BeautyQuests.logger.warning("Sound " + finishSound + " is not a valid Bukkit sound.");
		}
		
		// stageDescription
		itemNameColor = config.getString("itemNameColor");
		itemAmountColor = config.getString("itemAmountColor");
		stageDescriptionFormat = config.getString("stageDescriptionFormat");
		descPrefix = "{nl}" + config.getString("stageDescriptionItemsSplit.prefix");
		descAmountFormat = config.getString("stageDescriptionItemsSplit.amountFormat");
		descXOne = config.getBoolean("stageDescriptionItemsSplit.showXOne");
		inlineAlone = config.getBoolean("stageDescriptionItemsSplit.inlineAlone");
		for (String s : config.getStringList("stageDescriptionItemsSplit.sources")){
			try{
				descSources.add(Source.valueOf(s));
			}catch (IllegalArgumentException ex){
				BeautyQuests.logger.warning("Loading of description splitted sources failed : source " + s + " does not exist");
				continue;
			}
		}
		
		questDescription = new QuestDescription(config.getConfigurationSection("questDescription"));
		
		if (NMS.isValid()) {
			particleStart = loadParticles(config, "start", new Particle(ParticleEffect.REDSTONE, ParticleShape.POINT, new OrdinaryColor(Color.YELLOW)));
			particleTalk = loadParticles(config, "talk", new Particle(ParticleEffect.VILLAGER_HAPPY, ParticleShape.BAR, null));
			particleNext = loadParticles(config, "next", new Particle(ParticleEffect.SMOKE_NORMAL, ParticleShape.SPOT, null));
		}

		holoLaunchItem = loadHologram("launchItem");
		holoLaunchNoItem = loadHologram("nolaunchItem");
		holoTalkItem = loadHologram("talkItem");
	}
	
	private Particle loadParticles(FileConfiguration config, String name, Particle defaultParticle) {
		Particle particle = null;
		if (config.getBoolean(name + ".enabled")) {
			try{
				particle = Particle.deserialize(config.getConfigurationSection(name).getValues(false));
			}catch (Exception ex){
				BeautyQuests.logger.warning("Loading of " + name + " particles failed: Invalid particle, color or shape.");
				particle = defaultParticle;
			}
			BeautyQuests.logger.info("Loaded " + name + " particles: " + particle.toString());
		}
		return particle;
	}
	
	private ItemStack loadHologram(String name) {
		if (BeautyQuests.getInstance().getDataFile().contains(name)){
			return ItemStack.deserialize(BeautyQuests.getInstance().getDataFile().getConfigurationSection(name).getValues(false));
		}
		return null;
	}
	
	private boolean migrateEntry(ConfigurationSection config, ConfigurationSection migrateFrom, String key, String migrateKey) {
		if (migrateFrom.contains(migrateKey)) {
			config.set(key, migrateFrom.get(migrateKey));
			migrateFrom.set(migrateKey, null);
			return true;
		}
		return false;
	}

	public static String getPrefix(){
		return (enablePrefix) ? Lang.Prefix.toString() : "§6";
	}

	public static int getTimeBetween(){
		return timer;
	}
	
	public static String getLanguage() {
		return lang;
	}
	
	public static int getMaxLaunchedQuests() {
		return maxLaunchedQuests;
	}

	public static boolean sendQuestUpdateMessage(){
		return sendUpdate;
	}

	public static boolean sendStageStartMessage(){
		return stageStart;
	}

	public static boolean allowPlayerCancelQuest(){
		return playerCancelQuest;
	}

	public static boolean questConfirmGUI(){
		return questConfirmGUI;
	}
	
	public static boolean playSounds(){
		return sounds;
	}

	public static boolean doFireworks(){
		return fireworks;
	}
	
	public static boolean showMobsProgressBar() {
		return mobsProgressBar && QuestsAPI.hasBossBarManager();
	}
	
	public static int getProgressBarTimeout(){
		return progressBarTimeoutSeconds;
	}
	
	public static ClickType getNPCClick() {
		return npcClick;
	}
	
	public static boolean handleGPS(){
		return gps;
	}
	
	public static boolean xpOverridedSkillAPI(){
		return skillAPIoverride;
	}

	public static boolean showScoreboards(){
		return scoreboard;
	}

	public static ItemStack getItemMaterial() {
		return item;
	}
	
	public static XMaterial getPageMaterial(){
		return  pageItem;
	}
	
	public static int getRequirementUpdateTime() {
		return requirementUpdateTime;
	}
	
	public static boolean isRequirementReasonSentOnMultipleQuests() {
		return requirementReasonOnMultipleQuests;
	}
	
	public static boolean doesMenuOpenNotStartedTabWhenEmpty() {
		return menuOpenNotStartedTabWhenEmpty;
	}
	
	public static boolean hasStageEndRewardsMessage() {
		return stageEndRewardsMessage;
	}

	public static int getStartParticleDistance() {
		return startParticleDistance;
	}

	public static int getStartParticleDistanceSquared() {
		return startParticleDistanceSquared;
	}
	
	public static boolean isTextHologramDisabled(){
		return disableTextHologram;
	}
	
	public static String getItemAmountColor() {
		return itemAmountColor;
	}
	
	public static String getStageDescriptionFormat() {
		return stageDescriptionFormat;
	}
	
	public static String getItemNameColor() {
		return itemNameColor;
	}
	
	public static boolean showStartParticles(){
		return particleStart != null;
	}
	
	public static ParticleEffect.Particle getParticleStart(){
		return particleStart;
	}
	
	public static boolean showTalkParticles(){
		return particleTalk != null;
	}
	
	public static ParticleEffect.Particle getParticleTalk(){
		return particleTalk;
	}
	
	public static boolean showNextParticles(){
		return particleNext != null;
	}
	
	public static ParticleEffect.Particle getParticleNext(){
		return particleNext;
	}
	
	public static double getHologramsHeight(){
		return hologramsHeight;
	}
	
	public static boolean isCustomHologramNameShown(){
		return showCustomHologramName;
	}

	public static ItemStack getHoloLaunchItem(){
		return holoLaunchItem;
	}

	public static ItemStack getHoloLaunchNoItem(){
		return holoLaunchNoItem;
	}

	public static ItemStack getHoloTalkItem(){
		return holoTalkItem;
	}
	
	public static boolean hookAccounts(){
		return hookAcounts;
	}
	
	public static String dynmapSetName(){
		return dSetName;
	}
	
	public static String dynmapMarkerIcon(){
		return dIcon;
	}
	
	public static int dynmapMinimumZoom(){
		return dMinZoom;
	}
	
	public static boolean isMinecraftTranslationsEnabled() {
		return minecraftTranslationsFile != null && !minecraftTranslationsFile.isEmpty();
	}
	
	public static String getDescriptionItemPrefix(){
		return descPrefix;
	}
	
	public static String getDescriptionAmountFormat() {
		return descAmountFormat;
	}
	
	public static boolean showDescriptionItemsXOne(Source source){
		return splitDescription(source) && descXOne;
	}
	
	public static boolean inlineAlone() {
		return inlineAlone;
	}

	public static boolean splitDescription(Source source){
		if (source == Source.FORCESPLIT) return true;
		if (source == Source.FORCELINE) return false;
		return descSources.contains(source);
	}
	
	public static QuestDescription getQuestDescription() {
		return questDescription;
	}
	
	public static String getFinishSound(){
		return finishSound;
	}
	
	public static DialogsConfig getDialogsConfig() {
		return BeautyQuests.getInstance().getConfiguration().dialogs;
	}
	
	public enum ClickType {
		RIGHT, LEFT, ANY;
		
		public boolean applies(ClickType type) {
			return (this == type) || (this == ANY) || (type == ANY);
		}
	}
	
	public class DialogsConfig {
		
		private boolean inActionBar = false;
		private int defaultTime = 100;
		private boolean defaultSkippable = true;
		private boolean disableClick = false;
		private boolean history = true;
		
		private ConfigurationSection config;
		
		private DialogsConfig(ConfigurationSection config) {
			this.config = config;
		}
		
		private boolean update() {
			boolean result = false;
			if (config.getParent() != null) {
				result |= migrateEntry(config, config.getParent(), "inActionBar", "dialogsInActionBar");
				result |= migrateEntry(config, config.getParent(), "defaultTime", "dialogsDefaultTime");
				result |= migrateEntry(config, config.getParent(), "disableClick", "disableDialogClick");
				result |= migrateEntry(config, config.getParent(), "history", "dialogHistory");
			}
			return result;
		}
		
		private void init() {
			inActionBar = NMS.getMCVersion() > 8 && config.getBoolean("inActionBar");
			defaultTime = config.getInt("defaultTime");
			defaultSkippable = config.getBoolean("defaultSkippable");
			disableClick = config.getBoolean("disableClick");
			history = config.getBoolean("history");
		}
		
		public boolean sendInActionBar() {
			return inActionBar;
		}
		
		public int getDefaultTime() {
			return defaultTime;
		}
		
		public boolean isSkippableByDefault() {
			return defaultSkippable;
		}
		
		public boolean isClickDisabled() {
			return disableClick;
		}
		
		public boolean isHistoryEnabled() {
			return history;
		}
		
	}
	
}
