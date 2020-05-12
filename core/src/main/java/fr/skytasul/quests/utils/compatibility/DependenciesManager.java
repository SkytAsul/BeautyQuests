package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.utils.compatibility.mobs.BossAPI;
import fr.skytasul.quests.utils.compatibility.mobs.EpicBosses;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;

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
	
	public static void initialize() {
		if (testCompatibility("WorldGuard")){
			wg = true;
		}
		if (testCompatibility("MythicMobs")){
			mm = true;
			QuestsAPI.registerMobFactory(new MythicMobs());
		}
		if (testCompatibility("Vault")){
			vault = true;
		}
		if (testCompatibility("PlaceholderAPI")){
			papi = true;
			PlaceholderAPI.registerPlaceholders();
		}
		if (testCompatibility("SkillAPI")){
			skapi = true;
		}
		if (testCompatibility("HolographicDisplays")){
			holod = true;
		}
		if (testCompatibility("Jobs")){
			jobs = true;
		}
		if (testCompatibility("Factions")){
			fac = true;
		}
		if (testCompatibility("AccountsHook")){
			acc = true;
		}
		if (testCompatibility("dynmap")){
			dyn = true;
		}
		if (testCompatibility("Parties")){
			par = true; // not used currently
		}
		if (testCompatibility("EpicBosses")){
			eboss = true;
			Bukkit.getPluginManager().registerEvents(new EpicBosses(), BeautyQuests.getInstance());
		}
		if (testCompatibility("GPS")){
			gps = true;
			GPS.init();
		}
		if (testCompatibility("mcMMO")){
			mmo = true;
		}
		if (testCompatibility("McCombatLevel")){
			mclvl = true;
		}
		if (testCompatibility("Boss")) {
			boss = true;
			QuestsAPI.registerMobFactory(new BossAPI());
		}
	}
	
	private static boolean testCompatibility(String pluginName) {
		if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) return false;
		BeautyQuests.logger.info("Hooked into " + pluginName);
		return true;
	}
	
}
