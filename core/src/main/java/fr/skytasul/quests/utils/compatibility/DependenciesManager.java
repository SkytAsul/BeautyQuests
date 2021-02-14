package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.utils.compatibility.mobs.BossAPI;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;
import fr.skytasul.quests.utils.compatibility.worldguard.BQWorldGuard;

public class DependenciesManager {

	public static boolean wg = false; //		WorldGuard
	public static boolean mm = false; //	MythicMobs
	public static boolean vault = false; //	Vault
	public static boolean papi = false; //	PlaceholderAPI
	public static boolean skapi = false; //	SkillAPI
	public static boolean holod = false; //	HolographicDisplays
	public static boolean jobs = false; //	Jobs
	public static boolean fac = false; //		Factions
	public static boolean acc = false; //		AccountsHook
	public static boolean dyn = false; //	dynmap
	public static boolean par = false; //		Parties
	public static boolean eboss = false; //	EpicBosses
	public static boolean gps = false; //		GPS
	public static boolean mmo = false; //	mcMMO
	public static boolean mclvl = false; //	McCombatLevel
	public static boolean boss = false; //	Boss
	public static boolean cmi = false; //	CMI
	
	public static void testCompatibilities() {
		wg = testCompatibility("WorldGuard");
		mm = testCompatibility("MythicMobs");
		vault = testCompatibility("Vault");
		papi = testCompatibility("PlaceholderAPI");
		skapi = testCompatibility("SkillAPI");
		holod = testCompatibility("HolographicDisplays");
		jobs = testCompatibility("Jobs");
		fac = testCompatibility("Factions");
		acc = testCompatibility("AccountsHook");
		dyn = testCompatibility("dynmap");
		par = testCompatibility("Parties");
		/*if (testCompatibility("EpicBosses")){
			eboss = true;
			Bukkit.getPluginManager().registerEvents(new EpicBosses(), BeautyQuests.getInstance());
		}*/
		gps = testCompatibility("GPS");
		mmo = testCompatibility("mcMMO");
		mclvl = testCompatibility("McCombatLevel");
		boss = testCompatibility("Boss");
		cmi = testCompatibility("CMI");
	}

	public static void initializeCompatibilities() {
		if (wg) BQWorldGuard.init();
		if (mm) QuestsAPI.registerMobFactory(new MythicMobs());
		if (papi) QuestsPlaceholders.registerPlaceholders(BeautyQuests.getInstance().getConfig().getConfigurationSection("startedQuestsPlaceholder"));
		// eboss
		if (gps) GPS.init();
		if (boss) QuestsAPI.registerMobFactory(new BossAPI());
		if (cmi && BQCMI.areHologramsEnabled()) QuestsAPI.setHologramsManager(new BQCMI());
		if (holod) QuestsAPI.setHologramsManager(new BQHolographicDisplays());
	}
	
	private static boolean testCompatibility(String pluginName) {
		if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) return false;
		BeautyQuests.logger.info("Hooked into " + pluginName);
		return true;
	}
	
}
