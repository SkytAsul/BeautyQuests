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
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.requirements.ClassRequirement;
import fr.skytasul.quests.requirements.FactionRequirement;
import fr.skytasul.quests.requirements.JobLevelRequirement;
import fr.skytasul.quests.requirements.McCombatLevelRequirement;
import fr.skytasul.quests.requirements.McMMOSkillRequirement;
import fr.skytasul.quests.requirements.MoneyRequirement;
import fr.skytasul.quests.requirements.PlaceholderRequirement;
import fr.skytasul.quests.requirements.SkillAPILevelRequirement;
import fr.skytasul.quests.rewards.MoneyReward;
import fr.skytasul.quests.rewards.PermissionReward;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.compatibility.items.BQMMOItems;
import fr.skytasul.quests.utils.compatibility.maps.BQBlueMap;
import fr.skytasul.quests.utils.compatibility.maps.BQDynmap;
import fr.skytasul.quests.utils.compatibility.mobs.BQAdvancedSpawners;
import fr.skytasul.quests.utils.compatibility.mobs.BQBoss;
import fr.skytasul.quests.utils.compatibility.mobs.BQLevelledMobs;
import fr.skytasul.quests.utils.compatibility.mobs.BQWildStacker;
import fr.skytasul.quests.utils.compatibility.mobs.CitizensFactory;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs;
import fr.skytasul.quests.utils.compatibility.mobs.MythicMobs5;
import fr.skytasul.quests.utils.compatibility.npcs.BQCitizens;
import fr.skytasul.quests.utils.compatibility.npcs.BQSentinel;
import fr.skytasul.quests.utils.compatibility.npcs.BQServerNPCs;
import fr.skytasul.quests.utils.compatibility.worldguard.BQWorldGuard;

public class DependenciesManager implements Listener {
	
	public static final BQDependency znpcs = new BQDependency("ServersNPC", () -> QuestsAPI.setNPCsManager(new BQServerNPCs()), null, plugin -> {
		if (plugin.getClass().getName().equals("io.github.znetworkw.znpcservers.ServersNPC")) // NOSONAR
			return true;

		BeautyQuests.logger.warning("Your version of znpcs (" + plugin.getDescription().getVersion() + ") is not supported by BeautyQuests.");
		return false;
	});
	
	public static final BQDependency citizens = new BQDependency("Citizens", () -> {
		QuestsAPI.setNPCsManager(new BQCitizens());
		QuestsAPI.registerMobFactory(new CitizensFactory());
	});
	
	public static final BQDependency vault = new BQDependency("Vault", () -> {
		QuestsAPI.getRewards().register(new RewardCreator("moneyReward", MoneyReward.class, ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), MoneyReward::new));
		QuestsAPI.getRewards().register(new RewardCreator("permReward", PermissionReward.class, ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), PermissionReward::new));
		QuestsAPI.getRequirements().register(new RequirementCreator("moneyRequired", MoneyRequirement.class, ItemUtils.item(XMaterial.EMERALD, Lang.RMoney.toString()), MoneyRequirement::new));
	});
	
	public static final BQDependency papi = new BQDependency("PlaceholderAPI", () -> {
		QuestsPlaceholders.registerPlaceholders(BeautyQuests.getInstance().getConfig().getConfigurationSection("startedQuestsPlaceholder"));
		QuestsAPI.getRequirements().register(new RequirementCreator("placeholderRequired", PlaceholderRequirement.class, ItemUtils.item(XMaterial.NAME_TAG, Lang.RPlaceholder.toString()), PlaceholderRequirement::new));
	});
	
	public static final BQDependency skapi = new BQDependency("SkillAPI", () -> {
		QuestsAPI.getRequirements().register(new RequirementCreator("classRequired", ClassRequirement.class, ItemUtils.item(XMaterial.GHAST_TEAR, Lang.RClass.toString()), ClassRequirement::new));
		QuestsAPI.getRequirements().register(new RequirementCreator("skillAPILevelRequired", SkillAPILevelRequirement.class, ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RSkillAPILevel.toString()), SkillAPILevelRequirement::new));
	}).addPluginName("ProSkillAPI");
	
	public static final BQDependency cmi = new BQDependency("CMI", () -> {
		if (BQCMI.areHologramsEnabled()) QuestsAPI.setHologramsManager(new BQCMI());
	});
	
	public static final BQDependency boss = new BQDependency("Boss", () -> QuestsAPI.registerMobFactory(new BQBoss()), null, plugin -> {
		try {
			Class.forName("org.mineacademy.boss.model.Boss");
		}catch (ClassNotFoundException ex) {
			BeautyQuests.logger.warning("Your version of Boss (" + plugin.getDescription().getVersion() + ") is not compatible with BeautyQuests.");
			return false;
		}
		return true;
	});
	
	public static final BQDependency mm = new BQDependency("MythicMobs", () -> {
		try {
			Class.forName("io.lumine.mythic.api.MythicPlugin");
			QuestsAPI.registerMobFactory(new MythicMobs5());
		}catch (ClassNotFoundException ex) {
			QuestsAPI.registerMobFactory(new MythicMobs());
		}
	});
	
	public static final BQDependency advancedspawners = new BQDependency("AdvancedSpawners", () -> QuestsAPI.registerMobFactory(new BQAdvancedSpawners()));
	public static final BQDependency LevelledMobs =
			new BQDependency("LevelledMobs", () -> QuestsAPI.registerMobFactory(new BQLevelledMobs()));
	
	public static final BQDependency holod2 = new BQDependency("HolographicDisplays", () -> QuestsAPI.setHologramsManager(new BQHolographicDisplays2()), null, plugin -> plugin.getClass().getName().equals("com.gmail.filoghost.holographicdisplays.HolographicDisplays"));
	public static final BQDependency holod3 = new BQDependency("HolographicDisplays", () -> QuestsAPI.setHologramsManager(new BQHolographicDisplays3()), null, plugin -> {
		if (!plugin.getClass().getName().equals("me.filoghost.holographicdisplays.plugin.HolographicDisplays")) // NOSONAR
			return false;
		
		try {
			Class.forName("me.filoghost.holographicdisplays.api.HolographicDisplaysAPI");
			return true;
		}catch (ClassNotFoundException ex) {
			BeautyQuests.logger.warning("Your version of HolographicDisplays is unsupported. Please make sure you are running the LATEST dev build of HolographicDisplays.");
			return false;
		}
	});
	public static final BQDependency decentholograms = new BQDependency("DecentHolograms", () -> QuestsAPI.setHologramsManager(new BQDecentHolograms()));
	
	public static final BQDependency sentinel = new BQDependency("Sentinel", BQSentinel::initialize);
	
	public static final BQDependency wg =
			new BQDependency("WorldGuard", BQWorldGuard::initialize, BQWorldGuard::unload);
	public static final BQDependency jobs = new BQDependency("Jobs", () -> QuestsAPI.getRequirements().register(new RequirementCreator("jobLevelRequired", JobLevelRequirement.class, ItemUtils.item(XMaterial.LEATHER_CHESTPLATE, Lang.RJobLvl.toString()), JobLevelRequirement::new)));
	public static final BQDependency fac = new BQDependency("Factions", () -> QuestsAPI.getRequirements().register(new RequirementCreator("factionRequired", FactionRequirement.class, ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.RFaction.toString()), FactionRequirement::new)));
	public static final BQDependency acc = new BQDependency("AccountsHook");
	public static final BQDependency dyn = new BQDependency("dynmap", () -> QuestsAPI.registerQuestsHandler(new BQDynmap()));
	public static final BQDependency BlueMap = new BQDependency("BlueMap", () -> QuestsAPI.registerQuestsHandler(new BQBlueMap()));
	public static final BQDependency gps = new BQDependency("GPS", GPS::init);
	public static final BQDependency mmo = new BQDependency("mcMMO", () -> QuestsAPI.getRequirements().register(new RequirementCreator("mcmmoSklillLevelRequired", McMMOSkillRequirement.class, ItemUtils.item(XMaterial.IRON_CHESTPLATE, Lang.RSkillLvl.toString()), McMMOSkillRequirement::new)));
	public static final BQDependency mclvl = new BQDependency("McCombatLevel", () -> QuestsAPI.getRequirements().register(new RequirementCreator("mcmmoCombatLevelRequirement", McCombatLevelRequirement.class, ItemUtils.item(XMaterial.IRON_SWORD, Lang.RCombatLvl.toString()), McCombatLevelRequirement::new)));
	public static final BQDependency tokenEnchant = new BQDependency("TokenEnchant", () -> Bukkit.getPluginManager().registerEvents(new BQTokenEnchant(), BeautyQuests.getInstance()));
	public static final BQDependency ultimateTimber = new BQDependency("UltimateTimber", () -> Bukkit.getPluginManager().registerEvents(new BQUltimateTimber(), BeautyQuests.getInstance()));
	public static final BQDependency PlayerBlockTracker = new BQDependency("PlayerBlockTracker");
	public static final BQDependency WildStacker = new BQDependency("WildStacker", BQWildStacker::initialize);
	public static final BQDependency ItemsAdder =
			new BQDependency("ItemsAdder", BQItemsAdder::initialize, BQItemsAdder::unload);
	public static final BQDependency MMOItems =
			new BQDependency("MMOItems", BQMMOItems::initialize, BQMMOItems::unload);
	
	//public static final BQDependency par = new BQDependency("Parties");
	//public static final BQDependency eboss = new BQDependency("EpicBosses", () -> Bukkit.getPluginManager().registerEvents(new EpicBosses(), BeautyQuests.getInstance()));
	//public static final BQDependency interactions = new BQDependency("Interactions", () -> InteractionsAPI.); TODO
	
	private List<BQDependency> dependencies;
	private boolean dependenciesTested = false;
	private boolean dependenciesInitialized = false;
	private boolean lockDependencies = false;
	
	public DependenciesManager() {
		dependencies = new ArrayList<>(Arrays.asList(
				/*par, eboss, */
				znpcs, citizens, // npcs
				wg, gps, tokenEnchant, ultimateTimber, sentinel, PlayerBlockTracker, // other
				mm, boss, advancedspawners, LevelledMobs, WildStacker, // mobs
				vault, papi, acc, // hooks
				skapi, jobs, fac, mmo, mclvl, // rewards and requirements
				dyn, BlueMap, // maps
				cmi, holod2, holod3, decentholograms, // holograms
				ItemsAdder, MMOItems // items
				));
	}
	
	public List<BQDependency> getDependencies() {
		return dependencies;
	}
	
	public void addDependency(BQDependency dependency) {
		if (lockDependencies) {
			BeautyQuests.logger.severe("Trying to add a BQ dependency for plugin " + dependency.pluginNames + " after final locking.");
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
		dependencies.stream().filter(x -> !x.enabled && x.isPlugin(e.getPlugin())).findAny().ifPresent(dependency -> {
			if (dependency.testCompatibility(true) && dependenciesInitialized) dependency.initialize();
		});
	}
	
	public static class BQDependency {
		private final List<String> pluginNames;
		private final Runnable initialize;
		private final Runnable disable;
		private final Predicate<Plugin> isValid;
		private boolean enabled = false;
		private boolean forceDisable = false;
		private boolean initialized = false;
		private Plugin foundPlugin;
		
		public BQDependency(String pluginName) {
			this(pluginName, null);
		}
		
		public BQDependency(String pluginName, Runnable initialize) {
			this(pluginName, initialize, null, null);
		}
		
		public BQDependency(String pluginName, Runnable initialize, Runnable disable) {
			this(pluginName, initialize, disable, null);
		}

		public BQDependency(String pluginName, Runnable initialize, Runnable disable, Predicate<Plugin> isValid) {
			Validate.notNull(pluginName);
			this.pluginNames = new ArrayList<>();
			this.pluginNames.add(pluginName);
			this.initialize = initialize;
			this.disable = disable;
			this.isValid = isValid;
		}
		
		public BQDependency addPluginName(String name) {
			pluginNames.add(name);
			return this;
		}
		
		boolean isPlugin(Plugin plugin) {
			return pluginNames.contains(plugin.getName());
		}
		
		boolean testCompatibility(boolean after) {
			if (forceDisable) return false;
			Plugin plugin = pluginNames.stream().map(Bukkit.getPluginManager()::getPlugin).filter(x -> x != null && x.isEnabled()).findAny().orElse(null);
			if (plugin == null) return false;
			if (isValid != null && !isValid.test(plugin)) return false;
			DebugUtils.logMessage("Hooked into " + pluginNames + " v" + plugin.getDescription().getVersion() + (after ? " after primary initialization" : ""));
			enabled = true;
			foundPlugin = plugin;
			return true;
		}
		
		void initialize() {
			try {
				if (initialize != null) initialize.run();
				initialized = true;
			}catch (Throwable ex) {
				BeautyQuests.logger.severe("An error occurred while initializing " + pluginNames.toString() + " integration", ex);
				enabled = false;
			}
		}
		
		public void disable() {
			forceDisable = true;
			if (enabled) {
				enabled = false;
				if (disable != null && initialized) disable.run();
				initialized = false;
			}
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public Plugin getFoundPlugin() {
			if (!enabled)
				throw new IllegalStateException(
						"The dependency " + pluginNames + " is not enabled");
			return foundPlugin;
		}

	}
	
}
