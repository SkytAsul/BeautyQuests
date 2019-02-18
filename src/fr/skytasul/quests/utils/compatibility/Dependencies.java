package fr.skytasul.quests.utils.compatibility;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginManager;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.utils.compatibility.mobs.EpicBosses;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;

public class Dependencies {

	public static boolean wg = true; //		WorldGuard
	public static boolean mm = true; //	MythicMobs
	public static boolean vault = true; //	Vault
	public static boolean papi = true; //	PlaceholderAPI
	public static boolean skapi = true; //	SkillAPI
	public static boolean holod = true; //	HolographicDisplays
	public static boolean jobs = true; //	Jobs
	public static boolean fac = true; //		Factions
	public static boolean acc = true; //		AccountsHook
	public static boolean dyn = true; //	dynmap
	public static boolean par = true; //		Parties
	public static boolean eboss = true; //	EpicBosses
	public static boolean gps = true; //		GPS
	public static boolean mmo = true; //	mcMMO
	public static boolean mclvl = true; //	McCombatLevel
	
	public static void initialize(PluginManager pman, Logger logger){
		if (!pman.isPluginEnabled("WorldGuard")){
			wg = false;
		}
		if (!pman.isPluginEnabled("MythicMobs")){
			logger.info("MythicMobs isn't loaded on this server. Some features are not enabled because of that.");
			mm = false;
		}else pman.registerEvents(new MythicMobs(), BeautyQuests.getInstance());
		if (!pman.isPluginEnabled("Vault")){
			logger.info("Vault isn't loaded on this server. Some features are not enabled because of that.");
			vault = false;
		}
		if (!pman.isPluginEnabled("PlaceholderAPI")){
			logger.info("PlaceHolderAPI isn't loaded on this server. Some features are not enabled because of that.");
			papi = false;
		}else PlaceholderAPI.registerPlaceholders();
		if (!pman.isPluginEnabled("SkillAPI")){
			skapi = false;
		}
		if (!pman.isPluginEnabled("HolographicDisplays")){
			logger.info("HolographicDisplays isn't loaded on this server. Some features are not enabled because of that.");
			holod = false;
		}
		if (!pman.isPluginEnabled("Jobs")){
			logger.info("Jobs isn't loaded on this server. Some features are not enabled because of that.");
			jobs = false;
		}
		if (!pman.isPluginEnabled("Factions")){
			logger.info("Factions isn't loaded on this server. Some features are not enabled because of that.");
			fac = false;
		}
		if (!pman.isPluginEnabled("AccountsHook")){
			acc = false;
		}else new Accounts();
		if (!pman.isPluginEnabled("dynmap")){
			logger.info("dynmap isn't loaded on this server. Some features are not enabled because of that.");
			dyn = false;
		}
		if (!pman.isPluginEnabled("Parties")){
			par = false; // not used currently
		}
		if (!pman.isPluginEnabled("EpicBosses")){
			eboss = false;
		}else pman.registerEvents(new EpicBosses(), BeautyQuests.getInstance());
		if (!pman.isPluginEnabled("GPS")){
			gps = false;
		}else GPS.init();
		if (!pman.isPluginEnabled("mcMMO")){
			logger.info("mcMMO isn't loaded on this server. Some features are not enabled because of that.");
			mmo = false;
		}
		if (!pman.isPluginEnabled("McCombatLevel")){
			if (mmo) logger.info("McCombatLevel isn't loaded on this server. Some features are not enabled because of that.");
			mclvl = false;
		}
	}
	
}
