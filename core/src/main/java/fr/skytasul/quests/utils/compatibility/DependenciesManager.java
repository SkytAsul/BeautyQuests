package fr.skytasul.quests.utils.compatibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.compatibility.mobs.BossAPI;
import fr.skytasul.quests.utils.compatibility.mobs.CitizensFactory;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;
import fr.skytasul.quests.utils.compatibility.npcs.BQCitizens;
import fr.skytasul.quests.utils.compatibility.npcs.BQServerNPCs;
import fr.skytasul.quests.utils.compatibility.worldguard.BQWorldGuard;

public class DependenciesManager implements Listener {
	
	public static final BQDependency znpcs = new BQDependency("ServersNPC", () -> QuestsAPI.setNPCsManager(new BQServerNPCs()), null, plugin -> {
		if (plugin.getClass().getName().equals("io.github.znetworkw.znpcservers.ServersNPC")) return true;
		BeautyQuests.logger.warning("Your version of znpcs (" + plugin.getDescription().getVersion() + ") is not supported by BeautyQuests.");
		return false;
	});
	public static final BQDependency citizens = new BQDependency("Citizens", () -> {
		QuestsAPI.setNPCsManager(new BQCitizens());
		QuestsAPI.registerMobFactory(new CitizensFactory());
	});
	public static final BQDependency wg = new BQDependency("WorldGuard", BQWorldGuard::init, () -> BQWorldGuard.getInstance().disable(), null);
	public static final BQDependency mm = new BQDependency("MythicMobs", () -> QuestsAPI.registerMobFactory(new MythicMobs()));
	public static final BQDependency vault = new BQDependency("Vault");
	public static final BQDependency papi = new BQDependency("PlaceholderAPI", () -> QuestsPlaceholders.registerPlaceholders(BeautyQuests.getInstance().getConfig().getConfigurationSection("startedQuestsPlaceholder")));
	public static final BQDependency skapi = new BQDependency("SkillAPI");
	public static final BQDependency jobs = new BQDependency("Jobs");
	public static final BQDependency fac = new BQDependency("Factions");
	public static final BQDependency acc = new BQDependency("AccountsHook");
	public static final BQDependency dyn = new BQDependency("dynmap");
	//public static final BQDependency par = new BQDependency("Parties");
	//public static final BQDependency eboss = new BQDependency("EpicBosses", () -> Bukkit.getPluginManager().registerEvents(new EpicBosses(), BeautyQuests.getInstance()));
	public static final BQDependency gps = new BQDependency("GPS", GPS::init);
	public static final BQDependency mmo = new BQDependency("mcMMO");
	public static final BQDependency mclvl = new BQDependency("McCombatLevel");
	public static final BQDependency boss = new BQDependency("Boss", () -> QuestsAPI.registerMobFactory(new BossAPI()));
	public static final BQDependency cmi = new BQDependency("CMI", () -> {
		if (BQCMI.areHologramsEnabled()) QuestsAPI.setHologramsManager(new BQCMI());
	});
	public static final BQDependency holod = new BQDependency("HolographicDisplays", () -> QuestsAPI.setHologramsManager(new BQHolographicDisplays()));
	public static final BQDependency tokenEnchant = new BQDependency("TokenEnchant", () -> Bukkit.getPluginManager().registerEvents(new BQTokenEnchant(), BeautyQuests.getInstance()));
	public static final BQDependency ultimateTimber = new BQDependency("UltimateTimber", () -> Bukkit.getPluginManager().registerEvents(new BQUltimateTimber(), BeautyQuests.getInstance()));
	//public static final BQDependency interactions = new BQDependency("Interactions", () -> InteractionsAPI.); TODO
	
	private List<BQDependency> dependencies;
	private boolean dependenciesTested = false;
	private boolean dependenciesInitialized = false;
	private boolean lockDependencies = false;
	
	public DependenciesManager() {
		dependencies = new ArrayList<>(Arrays.asList(znpcs, citizens, wg, mm, vault, papi, skapi, jobs, fac, acc, dyn, /*par, eboss, */gps, mmo, mclvl, boss, cmi, holod, tokenEnchant, ultimateTimber));
	}
	
	public List<BQDependency> getDependencies() {
		return dependencies;
	}
	
	public void addDependency(BQDependency dependency) {
		if (lockDependencies) {
			BeautyQuests.logger.severe("Trying to add a BQ dependency for plugin " + dependency.pluginName + " after final locking.");
			return;
		}
		dependencies.add(dependency);
		if (dependenciesTested) {
			if (dependency.testCompatibility(true) && dependenciesInitialized) dependency.initialize();
		}
	}
	
	public void testCompatibilities() {
		if (dependenciesTested) return;
		dependencies.forEach(x -> x.testCompatibility(false));
		dependenciesTested = true;
	}

	public void initializeCompatibilities() {
		if (dependenciesInitialized) return;
		dependencies.stream().filter(BQDependency::isEnabled).forEach(BQDependency::initialize);
		dependenciesInitialized = true;
	}
	
	public void disableCompatibilities() {
		dependencies.forEach(BQDependency::disable);
	}
	
	public void lockDependencies() {
		lockDependencies = true;
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent e) {
		if (lockDependencies) return;
		//if (dependenciesTested) return;
		dependencies.stream().filter(x -> !x.enabled && x.pluginName.equals(e.getPlugin().getName())).findAny().ifPresent(dependency -> {
			if (dependency.testCompatibility(true) && dependenciesInitialized) dependency.initialize();
		});
	}
	
	public static class BQDependency {
		private final String pluginName;
		private final Runnable initialize;
		private final Runnable disable;
		private final Predicate<Plugin> isValid;
		private boolean enabled = false;
		private boolean forceDisable = false;
		
		public BQDependency(String pluginName) {
			this(pluginName, null);
		}
		
		public BQDependency(String pluginName, Runnable initialize) {
			this(pluginName, initialize, null, null);
		}
		
		public BQDependency(String pluginName, Runnable initialize, Runnable disable, Predicate<Plugin> isValid) {
			Validate.notNull(pluginName);
			this.pluginName = pluginName;
			this.initialize = initialize;
			this.disable = disable;
			this.isValid = isValid;
		}
		
		boolean testCompatibility(boolean after) {
			if (forceDisable) return false;
			Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
			if (plugin == null || !plugin.isEnabled()) return false;
			if (isValid != null && !isValid.test(plugin)) return false;
			DebugUtils.logMessage("Hooked into " + pluginName + " v" + plugin.getDescription().getVersion() + (after ? " after primary initialization" : ""));
			enabled = true;
			return true;
		}
		
		void initialize() {
			if (initialize != null) initialize.run();
		}
		
		public void disable() {
			forceDisable = true;
			if (enabled) {
				enabled = false;
				if (disable != null) disable.run();
			}
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
	}
	
}
