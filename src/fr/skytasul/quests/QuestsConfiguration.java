package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Color;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.OrdinaryColor;
import fr.skytasul.quests.utils.ParticleEffect.ParticleColor;
import fr.skytasul.quests.utils.ParticleEffect.ParticleLocation;
import fr.skytasul.quests.utils.ParticleEffect.ParticleProperty;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;
import fr.skytasul.quests.utils.compatibility.Dependencies;

public class QuestsConfiguration {

	private static int timer = 5;
	private static String minecraftTranslationsFile = null;
	private static int maxLaunchedQuests = 0;
	private static boolean sounds = true;
	private static boolean fireworks = true;
	private static boolean gps = false;
	private static boolean skillAPIoverride = true;
	private static boolean scoreboard = true;
	private static boolean emptyScoreboard = false;
	private static XMaterial item = XMaterial.BOOK;
	private static XMaterial pageItem = XMaterial.ARROW;
	private static String itemNameColor;
	private static String itemAmountColor;
	private static boolean enablePrefix = true;
	private static double hologramsHeight = 0.0;
	private static boolean disableTextHologram = false;
	private static ItemStack holoLaunchItem = null;
	private static ItemStack holoTalkItem = null;
	private static String questLore;
	/*private static ConfigurationSection effect;
	private static boolean effectEnabled;*/
	private static boolean mobsProgressBar = false;
	private static int progressBarTimeoutSeconds = 15;
	private static boolean hookAcounts = false;
	private static int splittedAdvancementPlaceholderMax = 3;
	private static boolean particles = true;
	private static ParticleEffect particle;
	private static OrdinaryColor particleColor;
	public static byte particleTypeCode;
	private static ParticleShape particleShape;
	private static List<ParticleLocation> particleLocations = new ArrayList<>();
	private static boolean sendUpdate = true;
	private static boolean stageStart = true;
	private static boolean playerCancelQuest = false;
	private static String dSetName = "Quests";
	private static String dIcon = "bookshelf";

	static int saveCycle = 15;
	static int firstQuestID = -1;

	
	static void initConfiguration(FileConfiguration config) {
		DebugUtils.debugMode = config.getBoolean("debug");
		if (DebugUtils.debugMode) BeautyQuests.logger.warning("Warning ! You are in debug mode");
		
		timer = config.getInt("redoMinuts");
		minecraftTranslationsFile = config.getString("minecraftTranslationsFile");
		if (isMinecraftTranslationsEnabled()) {
			if (BeautyQuests.MCversion >= 13) {
				if (!MinecraftNames.intialize(minecraftTranslationsFile)) {
					BeautyQuests.logger.warning("Cannot enable the \"minecraftTranslationsFile\" option : problem when initializing");
					minecraftTranslationsFile = null;
				}
			}else{
				BeautyQuests.logger.warning("Cannot enable the \"minecraftTranslationsFile\" option : only supported on Spigot 1.13 and higher");
				minecraftTranslationsFile = null;
			}
		}
		saveCycle = config.getInt("saveCycle");
		maxLaunchedQuests = config.getInt("maxLaunchedQuests");
		sendUpdate = config.getBoolean("playerQuestUpdateMessage");
		stageStart = config.getBoolean("playerStageStartMessage");
		playerCancelQuest = config.getBoolean("allowPlayerCancelQuest");
		particles = BeautyQuests.versionValid && config.getBoolean("particles");
		sounds = config.getBoolean("sounds");
		fireworks = config.getBoolean("fireworks");
		gps = Dependencies.gps && config.getBoolean("gps");
		skillAPIoverride = config.getBoolean("skillAPIoverride");
		scoreboard = config.getBoolean("scoreboards");
		emptyScoreboard = config.getBoolean("showEmptyScoreboard");
		item = XMaterial.fromString(config.getString("item"));
		pageItem = XMaterial.fromString(config.getString("pageItem"));
		if (item == null) item = XMaterial.BOOK;
		if (pageItem == null) pageItem = XMaterial.ARROW;
		itemNameColor = config.getString("itemNameColor");
		itemAmountColor = config.getString("itemAmountColor");
		mobsProgressBar = BeautyQuests.versionValid && config.getBoolean("mobsProgressBar");
		progressBarTimeoutSeconds = config.getInt("progressBarTimeoutSeconds");
		enablePrefix = config.getBoolean("enablePrefix");
		disableTextHologram = config.getBoolean("disableTextHologram");
		hologramsHeight = config.getDouble("hologramsHeight");
		if (BeautyQuests.data.contains("launchItem")){
			holoLaunchItem = ItemStack.deserialize(BeautyQuests.data.getConfigurationSection("launchItem").getValues(false));
		}else if (!StringUtils.isEmpty(config.getString("holoLaunchItemName"))){
			XMaterial xmat = XMaterial.fromString(config.getString("holoLaunchItemName"));
			if (xmat == null) xmat = XMaterial.BOOK;
			holoLaunchItem = xmat.parseItem();
		}
		if (BeautyQuests.data.contains("talkItem")){
			holoTalkItem = ItemStack.deserialize(BeautyQuests.data.getConfigurationSection("talkItem").getValues(false));
		}else if (!StringUtils.isEmpty(config.getString("holoTalkItemName"))){
			XMaterial xmat = XMaterial.fromString(config.getString("holoTalkItemName"));
			if (xmat == null) xmat = XMaterial.COAL;
			holoTalkItem = xmat.parseItem();
		}
		questLore = config.getString("questItem");
		splittedAdvancementPlaceholderMax = config.getInt("splittedAdvancementPlaceholderMax");
		hookAcounts = Dependencies.acc ? config.getBoolean("accountsHook") : false;
		if (hookAcounts) BeautyQuests.logger.info("AccountsHook is now managing player datas for quests !");
		dSetName = config.getString("dynmap.markerSetName");
		if (dSetName == null || dSetName.isEmpty()) Dependencies.dyn = false;
		dIcon = config.getString("dynmap.markerIcon");
		/*effect = config.getConfigurationSection("effectLib");
		effectEnabled = effect.getBoolean("enabled");*/
		if (particles){
			try{
				particle = ParticleEffect.fromName(config.getString("particleEffect"));
				particleColor = new ParticleEffect.OrdinaryColor(Color.deserialize(((MemorySection) config.get("particleColor")).getValues(false)));
				particleTypeCode = (byte) (particle == ParticleEffect.NOTE ? 2 : (particle.hasProperty(ParticleProperty.COLORABLE) ? 1 : 0));
				particleShape = ParticleShape.fromName(config.getString("particleShape"));
				particleLocations.clear();
				for (String s : config.getStringList("particleConfigLocations")) particleLocations.add(ParticleLocation.valueOf(s));
			}catch (Throwable ex){
				BeautyQuests.logger.warning("Invalid particle, color or shape.");
				particle = ParticleEffect.REDSTONE;
				particleColor = new ParticleEffect.OrdinaryColor(Color.YELLOW);
				particleTypeCode = 1;
				particleShape = ParticleShape.POINT;
				particleLocations.clear();
				particleLocations.add(ParticleLocation.START);
			}
			BeautyQuests.logger.info("Loaded particles (for location(s) " + Utils.itemsToFormattedString(Utils.arrayFromEnumList(particleLocations)) + "): \"" + particle.name() + "\" in shape \"" + particleShape.name().toLowerCase() + "\"" + (particleTypeCode != 0 ? " with color \"R" + (particleTypeCode == 1 ? particleColor.getRed() + " G" + particleColor.getGreen() + " B" + particleColor.getBlue() : "random") + "\"": ""));
		}
		
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

	public static boolean allowPlayerCancelQuest(){
		return playerCancelQuest;
	}
	
	public static boolean doParticles(){
		//if (nms == null) return false;
		return particles;
	}
	
	public static boolean doCustomParticles(ParticleLocation loc){
		if (!particles) return false;
		return particleLocations.contains(loc);
	}

	public static boolean playSounds(){
		return sounds;
	}

	public static boolean doFireworks(){
		return fireworks;
	}
	
	public static boolean showMobsProgressBar() {
		return mobsProgressBar;
	}
	
	public static int getProgressBarTimeout(){
		return progressBarTimeoutSeconds;
	}
	
	public static boolean handleGPS(){
		return gps;
	}
	
	public static boolean xpOverridedSkillAPI(){
		return skillAPIoverride;
	}

	public static boolean showScoreboards(){
		return scoreboard && BeautyQuests.versionValid;
	}

	public static boolean showEmptyScoreboards(){
		return emptyScoreboard;
	}

	public static XMaterial getItemMaterial(){
		return item;
	}
	
	public static XMaterial getPageMaterial(){
		return  pageItem;
	}
	
	public static boolean isTextHologramDisabled(){
		return disableTextHologram;
	}
	
	public static String getItemAmountColor() {
		return itemAmountColor;
	}
	
	public static String getItemNameColor() {
		return itemNameColor;
	}
	
	public static ParticleEffect getParticleEffect(){
		return particle;
	}
	
	public static ParticleColor getParticleColor(){
		return particleColor;
	}
	
	public static ParticleShape getParticleShape(){
		return particleShape;
	}
	
	public static double getHologramsHeight(){
		return hologramsHeight;
	}

	public static ItemStack getHoloLaunchItem(){
		if (!Dependencies.holod) return null;
		return holoLaunchItem;
	}

	public static ItemStack getHoloTalkItem(){
		if (!Dependencies.holod) return null;
		return holoTalkItem;
	}
	
	public static String getQuestItemLore(){
		return questLore;
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
	
	public static int getMaxSplittedAdvancementPlaceholder(){
		return splittedAdvancementPlaceholderMax;
	}
	
	public static boolean isMinecraftTranslationsEnabled() {
		return minecraftTranslationsFile != null;
	}
	
	/*public static boolean isEffectLibEnabled(){
		return elib ? effectEnabled : false;
	}*/
	
	/*public static ConfigurationSection getEffectConfig(){
		return effect;
	}*/
}
