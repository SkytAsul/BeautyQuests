package fr.skytasul.quests.utils.compatibility;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.compatibility.mobs.EpicBosses;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;

public class Dependencies {

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
	
	public static void initialize(PluginManager pman, Logger logger){
		if (pman.isPluginEnabled("WorldGuard")){
			wg = true;
		}
		if (pman.isPluginEnabled("MythicMobs")){
			mm = true;
			pman.registerEvents(new MythicMobs(), BeautyQuests.getInstance());
		}
		if (pman.isPluginEnabled("Vault")){
			vault = true;
		}
		if (pman.isPluginEnabled("PlaceholderAPI")){
			papi = true;
			PlaceholderAPI.registerPlaceholders();
		}
		if (pman.isPluginEnabled("SkillAPI")){
			skapi = true;
		}
		if (pman.isPluginEnabled("HolographicDisplays")){
			holod = true;
		}
		if (pman.isPluginEnabled("Jobs")){
			jobs = true;
		}
		if (pman.isPluginEnabled("Factions")){
			fac = true;
		}
		if (pman.isPluginEnabled("AccountsHook")){
			acc = true;
			new Accounts();
		}
		if (pman.isPluginEnabled("dynmap")){
			dyn = true;
		}
		if (pman.isPluginEnabled("Parties")){
			par = true; // not used currently
		}
		if (pman.isPluginEnabled("EpicBosses")){
			eboss = true;
			pman.registerEvents(new EpicBosses(), BeautyQuests.getInstance());
		}
		if (pman.isPluginEnabled("GPS")){
			gps = true;
			GPS.init();
		}
		if (pman.isPluginEnabled("mcMMO")){
			mmo = true;
		}
		if (pman.isPluginEnabled("McCombatLevel")){
			mclvl = true;
		}
	}
	
}
