package fr.skytasul.quests.integrations;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import fr.skytasul.quests.api.AbstractHolograms;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.utils.IntegrationManager;
import fr.skytasul.quests.api.utils.IntegrationManager.BQDependency;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.integrations.factions.FactionRequirement;
import fr.skytasul.quests.integrations.jobs.JobLevelRequirement;
import fr.skytasul.quests.integrations.maps.BQBlueMap;
import fr.skytasul.quests.integrations.maps.BQDynmap;
import fr.skytasul.quests.integrations.mcmmo.McCombatLevelRequirement;
import fr.skytasul.quests.integrations.mcmmo.McMMOSkillRequirement;
import fr.skytasul.quests.integrations.mobs.*;
import fr.skytasul.quests.integrations.npcs.*;
import fr.skytasul.quests.integrations.placeholders.PapiMessageProcessor;
import fr.skytasul.quests.integrations.placeholders.PlaceholderRequirement;
import fr.skytasul.quests.integrations.placeholders.QuestsPlaceholders;
import fr.skytasul.quests.integrations.skillapi.ClassRequirement;
import fr.skytasul.quests.integrations.skillapi.SkillAPILevelRequirement;
import fr.skytasul.quests.integrations.skillapi.SkillAPIXpReward;
import fr.skytasul.quests.integrations.vault.economy.MoneyRequirement;
import fr.skytasul.quests.integrations.vault.economy.MoneyReward;
import fr.skytasul.quests.integrations.vault.permission.PermissionReward;
import fr.skytasul.quests.integrations.worldguard.BQWorldGuard;

public class IntegrationsLoader {

	private static IntegrationsLoader instance;

	public static IntegrationsLoader getInstance() {
		return instance;
	}

	private IntegrationsConfiguration config;

	public IntegrationsLoader() {
		instance = this;

		config = new IntegrationsConfiguration(QuestsPlugin.getPlugin().getConfig());
		config.load();

		IntegrationManager manager = QuestsPlugin.getPlugin().getIntegrationManager();

		// NPCS
		manager.addDependency(new BQDependency("ServersNPC",
				() -> QuestsAPI.getAPI().addNpcFactory("znpcs", new BQServerNPCs()), null, this::isZnpcsVersionValid));

		manager.addDependency(new BQDependency("ZNPCsPlus", this::registerZnpcsPlus));

		manager.addDependency(new BQDependency("Citizens", () -> {
			QuestsAPI.getAPI().addNpcFactory("citizens", new BQCitizens());
			QuestsAPI.getAPI().registerMobFactory(new CitizensFactory());
		}));


		// MOBS
		manager.addDependency(new BQDependency("MythicMobs", this::registerMythicMobs));

		manager.addDependency(new BQDependency("Boss", () -> QuestsAPI.getAPI().registerMobFactory(new BQBoss()), null,
				this::isBossVersionValid));

		manager.addDependency(
				new BQDependency("AdvancedSpawners", () -> QuestsAPI.getAPI().registerMobFactory(new BQAdvancedSpawners())));

		manager.addDependency(
				new BQDependency("LevelledMobs", () -> QuestsAPI.getAPI().registerMobFactory(new BQLevelledMobs())));

		manager.addDependency(new BQDependency("WildStacker", BQWildStacker::initialize));


		// REWARDS / REQUIREMENTS
		manager.addDependency(new BQDependency("SkillAPI", this::registerSkillApi).addPluginName("ProSkillAPI"));
		manager.addDependency(new BQDependency("Jobs", this::registerJobs));
		manager.addDependency(new BQDependency("Factions", this::registerFactions));
		manager.addDependency(new BQDependency("mcMMO", this::registerMcMmo));
		manager.addDependency(new BQDependency("McCombatLevel", this::registerMcCombatLevel));
		manager.addDependency(new BQDependency("Vault", this::registerVault));


		// MAPS
		if (config.dynmapSetName() != null && !config.dynmapSetName().isEmpty()) {
			manager.addDependency(
					new BQDependency("dynmap", () -> QuestsAPI.getAPI().registerQuestsHandler(new BQDynmap())));
			manager.addDependency(
					new BQDependency("BlueMap", () -> QuestsAPI.getAPI().registerQuestsHandler(new BQBlueMap())));
		}


		// HOLOGRAMS
		manager.addDependency(new BQDependency("CMI", () -> {
			if (BQCMI.areHologramsEnabled())
				QuestsAPI.getAPI().setHologramsManager(new BQCMI());
		}));
		manager.addDependency(new BQDependency("HolographicDisplays", this::registerHolographicDisplays));
		manager.addDependency(
				new BQDependency("DecentHolograms", () -> QuestsAPI.getAPI().setHologramsManager(new BQDecentHolograms())));


		// OTHERS
		manager.addDependency(new BQDependency("PlaceholderAPI", this::registerPapi));
		manager.addDependency(new BQDependency("WorldGuard", BQWorldGuard::initialize, BQWorldGuard::unload));
		manager.addDependency(new BQDependency("Sentinel", BQSentinel::initialize));
		manager.addDependency(new BQDependency("TokenEnchant",
				() -> Bukkit.getPluginManager().registerEvents(new BQTokenEnchant(), QuestsPlugin.getPlugin())));
		manager.addDependency(new BQDependency("UltimateTimber",
				() -> Bukkit.getPluginManager().registerEvents(new BQUltimateTimber(), QuestsPlugin.getPlugin())));
		manager.addDependency(new BQDependency("ItemsAdder", BQItemsAdder::initialize, BQItemsAdder::unload));
		manager.addDependency(new BQDependency("MMOItems", BQMMOItems::initialize, BQMMOItems::unload));
	}

	private void registerPapi() {
		QuestsPlaceholders.registerPlaceholders(
				QuestsPlugin.getPlugin().getConfig().getConfigurationSection("startedQuestsPlaceholder"));
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("placeholderRequired", PlaceholderRequirement.class,
						ItemUtils.item(XMaterial.NAME_TAG, Lang.RPlaceholder.toString()), PlaceholderRequirement::new));
		QuestsAPI.getAPI().registerMessageProcessor("placeholderapi_replace", 5, new PapiMessageProcessor());
	}

	private void registerVault() {
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("moneyReward", MoneyReward.class,
				ItemUtils.item(XMaterial.EMERALD, Lang.rewardMoney.toString()), MoneyReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("permReward", PermissionReward.class,
				ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.rewardPerm.toString()), PermissionReward::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("moneyRequired", MoneyRequirement.class,
				ItemUtils.item(XMaterial.EMERALD, Lang.RMoney.toString()), MoneyRequirement::new));
	}

	private void registerHolographicDisplays() {
		AbstractHolograms<?> holograms;
		try {
			Class.forName("com.gmail.filoghost.holographicdisplays.HolographicDisplays"); // v2
			holograms = new BQHolographicDisplays2();
		} catch (ClassNotFoundException ex) {
			try {
				Class.forName("me.filoghost.holographicdisplays.plugin.HolographicDisplays"); // v3
				holograms = new BQHolographicDisplays3();
			} catch (ClassNotFoundException ex1) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning(
						"Your version of HolographicDisplays is unsupported. Please make sure you are running the LATEST dev build of HolographicDisplays.");
				return;
			}
		}
		QuestsAPI.getAPI().setHologramsManager(holograms);
	}

	private void registerMcCombatLevel() {
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("mcmmoCombatLevelRequirement", McCombatLevelRequirement.class,
						ItemUtils.item(XMaterial.IRON_SWORD, Lang.RCombatLvl.toString()), McCombatLevelRequirement::new));
	}

	private void registerMcMmo() {
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("mcmmoSklillLevelRequired", McMMOSkillRequirement.class, ItemUtils.item(XMaterial.IRON_CHESTPLATE, Lang.RSkillLvl.toString()), McMMOSkillRequirement::new));
	}

	private void registerFactions() {
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("factionRequired", FactionRequirement.class,
				ItemUtils.item(XMaterial.WITHER_SKELETON_SKULL, Lang.RFaction.toString()), FactionRequirement::new));
	}

	private void registerJobs() {
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("jobLevelRequired", JobLevelRequirement.class,
						ItemUtils.item(XMaterial.LEATHER_CHESTPLATE, Lang.RJobLvl.toString()),
						JobLevelRequirement::new));
	}

	private void registerSkillApi() {
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("classRequired", ClassRequirement.class,
				ItemUtils.item(XMaterial.GHAST_TEAR, Lang.RClass.toString()), ClassRequirement::new));
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("skillAPILevelRequired", SkillAPILevelRequirement.class,
						ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RSkillAPILevel.toString()),
						SkillAPILevelRequirement::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("skillAPI-exp", SkillAPIXpReward.class,
				ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RWSkillApiXp.toString()), SkillAPIXpReward::new));
	}

	private boolean isBossVersionValid(Plugin plugin) {
		try {
			Class.forName("org.mineacademy.boss.model.Boss");
		} catch (ClassNotFoundException ex) {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Your version of Boss ("
					+ plugin.getDescription().getVersion() + ") is not compatible with BeautyQuests.");
			return false;
		}
		return true;
	}

	private void registerMythicMobs() {
		try {
			Class.forName("io.lumine.mythic.api.MythicPlugin");
			QuestsAPI.getAPI().registerMobFactory(new MythicMobs5());
		} catch (ClassNotFoundException ex) {
			QuestsAPI.getAPI().registerMobFactory(new MythicMobs());
		}
	}

	private boolean isZnpcsVersionValid(Plugin plugin) {
		if (plugin.getClass().getName().equals("io.github.gonalez.znpcs.ServersNPC")) // NOSONAR
			return true;

		QuestsPlugin.getPlugin().getLoggerExpanded().warning("Your version of znpcs ("
				+ plugin.getDescription().getVersion() + ") is not supported by BeautyQuests.");
		return false;
	}

	private void registerZnpcsPlus() {
		try {
			Class.forName("lol.pyr.znpcsplus.api.NpcApiProvider");
			QuestsAPI.getAPI().addNpcFactory("znpcsplus", new BQZNPCsPlus());
		} catch (ClassNotFoundException ex) {
			QuestsAPI.getAPI().addNpcFactory("znpcsplus", new BQZNPCsPlusOld()); // TODO remove, old version of znpcs+

			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("Your version of ZNPCsPlus will soon not be supported by BeautyQuests.");
		}
	}

	public IntegrationsConfiguration getConfig() {
		return config;
	}

}
