package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import com.google.common.collect.Sets;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.options.description.QuestDescription;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.quests.PlayerListGUI.Category;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Accounts;
import fr.skytasul.quests.utils.compatibility.DependenciesManager;
import fr.skytasul.quests.utils.nms.NMS;

public class QuestsConfiguration {

	private static int timer = 5;
	private static String minecraftTranslationsFile = null;
	private static int maxLaunchedQuests = 0;
	private static boolean sounds = true;
	private static boolean fireworks = true;
	private static boolean gps = false;
	private static boolean skillAPIoverride = true;
	private static boolean scoreboard = true;
	private static String finishSound = "ENTITY_PLAYER_LEVELUP";
	private static String nextStageSound = "ITEM_FIRECHARGE_USE";
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
	private static boolean usePlayerBlockTracker = false;
	private static ParticleEffect particleStart;
	private static ParticleEffect particleTalk;
	private static ParticleEffect particleNext;
	private static boolean sendUpdate = true;
	private static boolean stageStart = true;
	private static boolean questConfirmGUI = false;
	private static Collection<ClickType> npcClicks = Arrays.asList(ClickType.RIGHT, ClickType.SHIFT_RIGHT);
	private static boolean skipNpcGuiIfOnlyOneQuest = true;
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
	private static boolean stageEndRewardsMessage = true;
	private static QuestDescription questDescription;
	
	private static ItemStack holoLaunchItem = null;
	private static ItemStack holoLaunchNoItem = null;
	private static ItemStack holoTalkItem = null;
	
	private static FireworkMeta defaultFirework = null;

	static boolean backups = true;
	
	static boolean saveCycleMessage = true;
	static int saveCycle = 15;
	static int firstQuestID = -1; // changed in 0.19, TODO
	
	private FileConfiguration config;
	private DialogsConfig dialogs;
	private QuestsMenuConfig menu;
	
	QuestsConfiguration(BeautyQuests plugin) {
		config = plugin.getConfig();
		dialogs = new DialogsConfig(config.getConfigurationSection("dialogs"));
		menu = new QuestsMenuConfig(config.getConfigurationSection("questsMenu"));
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public DialogsConfig getDialogs() {
		return dialogs;
	}
	
	public QuestsMenuConfig getQuestsMenu() {
		return menu;
	}
	
	boolean update() {
		boolean result = false;
		result |= dialogs.update();
		result |= menu.update();
		return result;
	}
	
	void init() {
		backups = config.getBoolean("backups", true);
		if (!backups) BeautyQuests.logger.warning("Backups are disabled due to the presence of \"backups: false\" in config.yml.");
		
		timer = config.getInt("redoMinuts");
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
		menu.init();
		
		saveCycle = config.getInt("saveCycle");
		saveCycleMessage = config.getBoolean("saveCycleMessage");
		firstQuestID = config.getInt("firstQuest", -1);
		maxLaunchedQuests = config.getInt("maxLaunchedQuests");
		sendUpdate = config.getBoolean("playerQuestUpdateMessage");
		stageStart = config.getBoolean("playerStageStartMessage");
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
		item = ItemUtils.clearVisibleAttributes(item);
		if (config.contains("pageItem")) pageItem = XMaterial.matchXMaterial(config.getString("pageItem")).orElse(XMaterial.ARROW);
		if (pageItem == null) pageItem = XMaterial.ARROW;
		startParticleDistance = config.getInt("startParticleDistance");
		startParticleDistanceSquared = startParticleDistance * startParticleDistance;
		requirementUpdateTime = config.getInt("requirementUpdateTime");
		requirementReasonOnMultipleQuests = config.getBoolean("requirementReasonOnMultipleQuests");
		stageEndRewardsMessage = config.getBoolean("stageEndRewardsMessage");
		mobsProgressBar = config.getBoolean("mobsProgressBar");
		progressBarTimeoutSeconds = config.getInt("progressBarTimeoutSeconds");
		try {
			if (config.isString("npcClick")) {
				String click = config.getString("npcClick");
				npcClicks = Arrays.asList(click.equals("ANY") ? ClickType.values() : new ClickType[] { ClickType.valueOf(click.toUpperCase()) });
			}else {
				npcClicks = config.getStringList("npcClick")
						.stream()
						.map(String::toUpperCase)
						.map(ClickType::valueOf)
						.collect(Collectors.toList());
			}
		}catch (IllegalArgumentException ex) {
			BeautyQuests.logger.warning("Unknown click type " + config.get("npcClick") + " for config entry \"npcClick\"");
		}
		skipNpcGuiIfOnlyOneQuest = config.getBoolean("skip npc gui if only one quest");
		enablePrefix = config.getBoolean("enablePrefix");
		disableTextHologram = config.getBoolean("disableTextHologram");
		showCustomHologramName = config.getBoolean("showCustomHologramName");
		hologramsHeight = 0.28 + config.getDouble("hologramsHeight");
		hookAcounts = DependenciesManager.acc.isEnabled() && config.getBoolean("accountsHook");
		if (hookAcounts) {
			Bukkit.getPluginManager().registerEvents(new Accounts(), BeautyQuests.getInstance());
			BeautyQuests.logger.info("AccountsHook is now managing player datas for quests !");
		}
		usePlayerBlockTracker = DependenciesManager.PlayerBlockTracker.isEnabled() && config.getBoolean("usePlayerBlockTracker");
		dSetName = config.getString("dynmap.markerSetName");
		if (dSetName == null || dSetName.isEmpty()) DependenciesManager.dyn.disable();
		dIcon = config.getString("dynmap.markerIcon");
		dMinZoom = config.getInt("dynmap.minZoom");
		finishSound = loadSound("finishSound");
		nextStageSound = loadSound("nextStageSound");
		
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
			}
		}
		
		questDescription = new QuestDescription(config.getConfigurationSection("questDescription"));
		
		if (NMS.getMCVersion() >= 9) {
			particleStart = loadParticles(config, "start", new ParticleEffect(Particle.REDSTONE, ParticleShape.POINT, Color.YELLOW));
			particleTalk = loadParticles(config, "talk", new ParticleEffect(Particle.VILLAGER_HAPPY, ParticleShape.BAR, null));
			particleNext = loadParticles(config, "next", new ParticleEffect(Particle.SMOKE_NORMAL, ParticleShape.SPOT, null));
		}

		holoLaunchItem = loadHologram("launchItem");
		holoLaunchNoItem = loadHologram("nolaunchItem");
		holoTalkItem = loadHologram("talkItem");
		
		if (BeautyQuests.getInstance().getDataFile().contains("firework")) {
			defaultFirework = BeautyQuests.getInstance().getDataFile().getSerializable("firework", FireworkMeta.class);
		}else {
			FireworkMeta fm = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(XMaterial.FIREWORK_ROCKET.parseMaterial());
			fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withTrail().withFlicker().withColor(Color.YELLOW, Color.ORANGE).withFade(Color.SILVER).build());
			fm.setPower(0);
			defaultFirework = fm;
		}
	}
	
	private ParticleEffect loadParticles(FileConfiguration config, String name, ParticleEffect defaultParticle) {
		ParticleEffect particle = null;
		if (config.getBoolean(name + ".enabled")) {
			try{
				particle = ParticleEffect.deserialize(config.getConfigurationSection(name));
			}catch (Exception ex){
				BeautyQuests.logger.warning("Loading of " + name + " particles failed: Invalid particle, color or shape.", ex);
			}
			if (particle == null) particle = defaultParticle;
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
	
	private String loadSound(String key) {
		String sound = config.getString(key);
		try {
			Sound.valueOf(sound.toUpperCase());
			sound = sound.toUpperCase();
		}catch (IllegalArgumentException ex) {
			BeautyQuests.logger.warning("Sound " + sound + " is not a valid Bukkit sound.");
		}
		return sound;
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
	
	public static int getMaxLaunchedQuests() {
		return maxLaunchedQuests;
	}

	public static boolean sendQuestUpdateMessage(){
		return sendUpdate;
	}

	public static boolean sendStageStartMessage(){
		return stageStart;
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
	
	public static Collection<ClickType> getNPCClicks() {
		return npcClicks;
	}
	
	public static boolean skipNpcGuiIfOnlyOneQuest() {
		return skipNpcGuiIfOnlyOneQuest;
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
	
	public static ParticleEffect getParticleStart() {
		return particleStart;
	}
	
	public static boolean showTalkParticles(){
		return particleTalk != null;
	}
	
	public static ParticleEffect getParticleTalk() {
		return particleTalk;
	}
	
	public static boolean showNextParticles(){
		return particleNext != null;
	}
	
	public static ParticleEffect getParticleNext() {
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
	
	public static FireworkMeta getDefaultFirework() {
		return defaultFirework;
	}
	
	public static boolean hookAccounts(){
		return hookAcounts;
	}
	
	public static boolean usePlayerBlockTracker() {
		return usePlayerBlockTracker;
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
	
	public static String getNextStageSound() {
		return nextStageSound;
	}
	
	public static DialogsConfig getDialogsConfig() {
		return BeautyQuests.getInstance().getConfiguration().dialogs;
	}
	
	public static QuestsMenuConfig getMenuConfig() {
		return BeautyQuests.getInstance().getConfiguration().menu;
	}
	
	public enum ClickType {
		RIGHT, SHIFT_RIGHT, LEFT, SHIFT_LEFT;
		
		public static ClickType of(boolean left, boolean shift) {
			if (left) {
				return shift ? SHIFT_LEFT : LEFT;
			}else {
				return shift ? SHIFT_RIGHT : RIGHT;
			}
		}
	}
	
	public class DialogsConfig {
		
		private boolean inActionBar = false;
		private int defaultTime = 100;
		private boolean defaultSkippable = true;
		private boolean disableClick = false;
		private boolean history = true;
		private int maxDistance = 15, maxDistanceSquared = 15 * 15;
		
		private String defaultPlayerSound = null;
		private String defaultNPCSound = null;
		
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
			maxDistance = config.getInt("maxDistance");
			maxDistanceSquared = maxDistance <= 0 ? 0 : (maxDistance * maxDistance);
			
			defaultPlayerSound = config.getString("defaultPlayerSound");
			defaultNPCSound = config.getString("defaultNPCSound");
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
		
		public int getMaxDistance() {
			return maxDistance;
		}
		
		public int getMaxDistanceSquared() {
			return maxDistanceSquared;
		}
		
		public String getDefaultPlayerSound() {
			return defaultPlayerSound;
		}
		
		public String getDefaultNPCSound() {
			return defaultNPCSound;
		}
		
	}
	
	public class QuestsMenuConfig {
		
		private Set<Category> tabs;
		private boolean openNotStartedTabWhenEmpty = true;
		private boolean allowPlayerCancelQuest = true;
		
		private ConfigurationSection config;
		
		private QuestsMenuConfig(ConfigurationSection config) {
			this.config = config;
		}
		
		private boolean update() {
			boolean result = false;
			if (config.getParent() != null) {
				result |= migrateEntry(config, config.getParent(), "openNotStartedTabWhenEmpty", "menuOpenNotStartedTabWhenEmpty");
				result |= migrateEntry(config, config.getParent(), "allowPlayerCancelQuest", "allowPlayerCancelQuest");
			}
			return result;
		}
		
		private void init() {
			tabs = config.getStringList("enabledTabs").stream().map(Category::fromString).collect(Collectors.toSet());
			if (tabs.isEmpty()) {
				BeautyQuests.logger.warning("Quests Menu must have at least one enabled tab.");
				tabs = Sets.newHashSet(Category.values());
			}
			openNotStartedTabWhenEmpty = config.getBoolean("openNotStartedTabWhenEmpty");
			allowPlayerCancelQuest = config.getBoolean("allowPlayerCancelQuest");
		}
		
		public boolean isNotStartedTabOpenedWhenEmpty() {
			return openNotStartedTabWhenEmpty;
		}
		
		public boolean allowPlayerCancelQuest() {
			return allowPlayerCancelQuest;
		}
		
		public Set<Category> getEnabledTabs() {
			return tabs;
		}
		
	}
	
}
