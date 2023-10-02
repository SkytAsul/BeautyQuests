package fr.skytasul.quests;

import java.util.*;
import java.util.stream.Collectors;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.NpcClickType;
import fr.skytasul.quests.api.options.description.DescriptionSource;
import fr.skytasul.quests.api.options.description.QuestDescription;
import fr.skytasul.quests.api.utils.*;
import fr.skytasul.quests.players.BqAccountsHook;
import fr.skytasul.quests.utils.ParticleEffect;
import fr.skytasul.quests.utils.ParticleEffect.ParticleShape;
import fr.skytasul.quests.utils.compatibility.InternalIntegrations;

public class QuestsConfigurationImplementation implements QuestsConfiguration {

	public static QuestsConfigurationImplementation getConfiguration() {
		return BeautyQuests.getInstance().getConfiguration();
	}

	private String minecraftTranslationsFile = null;
	private boolean enablePrefix = true;
	private double hologramsHeight = 0.0;
	private boolean disableTextHologram = false;
	private boolean showCustomHologramName = true;
	private boolean hookAcounts = false;
	private boolean usePlayerBlockTracker = false;
	private ParticleEffect particleStart;
	private ParticleEffect particleTalk;
	private ParticleEffect particleNext;

	private ItemStack holoLaunchItem = null;
	private ItemStack holoLaunchNoItem = null;
	private ItemStack holoTalkItem = null;

	private FireworkMeta defaultFirework = null;

	boolean backups = true;

	boolean saveCycleMessage = true;
	int saveCycle = 15;
	int firstQuestID = -1; // TODO migration 0.19

	private final FileConfiguration config;
	private QuestsConfig quests;
	private DialogsConfig dialogs;
	private QuestsMenuConfig menu;
	private StageDescriptionConfig stageDescription;
	private QuestDescriptionConfig questDescription;

	QuestsConfigurationImplementation(FileConfiguration config) {
		this.config = config;

		quests = new QuestsConfig();
		dialogs = new DialogsConfig(config.getConfigurationSection("dialogs"));
		menu = new QuestsMenuConfig(config.getConfigurationSection("questsMenu"));
		stageDescription = new StageDescriptionConfig(config.getConfigurationSection("stage description"));
		questDescription = new QuestDescriptionConfig(config.getConfigurationSection("questDescription"));
	}

	boolean update() {
		boolean result = false;
		result |= dialogs.update();
		result |= menu.update();
		result |= stageDescription.update();
		return result;
	}

	void init() {
		backups = config.getBoolean("backups", true);
		if (!backups) QuestsPlugin.getPlugin().getLoggerExpanded().warning("Backups are disabled due to the presence of \"backups: false\" in config.yml.");

		minecraftTranslationsFile = config.getString("minecraftTranslationsFile");
		if (isMinecraftTranslationsEnabled())
			initializeTranslations();
		quests.init();
		dialogs.init();
		menu.init();
		stageDescription.init();
		questDescription.init();

		saveCycle = config.getInt("saveCycle");
		saveCycleMessage = config.getBoolean("saveCycleMessage");
		firstQuestID = config.getInt("firstQuest", -1);
		enablePrefix = config.getBoolean("enablePrefix");
		disableTextHologram = config.getBoolean("disableTextHologram");
		showCustomHologramName = config.getBoolean("showCustomHologramName");
		hologramsHeight = 0.28 + config.getDouble("hologramsHeight");
		hookAcounts = config.getBoolean("accountsHook");
		if (hookAcounts) {
			Bukkit.getPluginManager().registerEvents(new BqAccountsHook(), BeautyQuests.getInstance());
			QuestsPlugin.getPlugin().getLoggerExpanded().info("AccountsHook is now managing player datas for quests !");
		}
		usePlayerBlockTracker = config.getBoolean("usePlayerBlockTracker");

		if (MinecraftVersion.MAJOR >= 9) {
			particleStart = loadParticles("start", new ParticleEffect(Particle.REDSTONE, ParticleShape.POINT, Color.YELLOW));
			particleTalk = loadParticles("talk", new ParticleEffect(Particle.VILLAGER_HAPPY, ParticleShape.BAR, null));
			particleNext = loadParticles("next", new ParticleEffect(Particle.SMOKE_NORMAL, ParticleShape.SPOT, null));
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

	private void initializeTranslations() {
		if (MinecraftVersion.MAJOR >= 13) {
			String fileName = minecraftTranslationsFile;
			Optional<String> extension = Utils.getFilenameExtension(minecraftTranslationsFile);
			if (extension.isPresent()) {
				if (extension.get().equalsIgnoreCase("json")) {
					QuestsPlugin.getPlugin().getLoggerExpanded().warning("File " + fileName + " is not a JSON file.");
					return;
				}
			} else {
				fileName += ".json";
			}

			if (!MinecraftNames.intialize(QuestsPlugin.getPlugin().getDataFolder().toPath().resolve(fileName))) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Cannot enable the \"minecraftTranslationsFile\" option : problem when initializing");
				minecraftTranslationsFile = null;
			}
		} else {
			QuestsPlugin.getPlugin().getLoggerExpanded().warning(
					"Cannot enable the \"minecraftTranslationsFile\" option : only supported on Spigot 1.13 and higher");
			minecraftTranslationsFile = null;
		}
	}

	private ParticleEffect loadParticles(String name, ParticleEffect defaultParticle) {
		ParticleEffect particle = null;
		if (config.getBoolean(name + ".enabled")) {
			try{
				particle = ParticleEffect.deserialize(config.getConfigurationSection(name));
			}catch (Exception ex){
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Loading of " + name + " particles failed: Invalid particle, color or shape.", ex);
			}
			if (particle == null) particle = defaultParticle;
			QuestsPlugin.getPlugin().getLoggerExpanded().info("Loaded " + name + " particles: " + particle.toString());
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
			QuestsPlugin.getPlugin().getLoggerExpanded().warning("Sound " + sound + " is not a valid Bukkit sound.");
		}
		return sound;
	}

	private boolean migrateEntry(ConfigurationSection oldConfig, String oldKey, ConfigurationSection newConfig,
			String newKey) {
		if (oldConfig.contains(oldKey)) {
			newConfig.set(newKey, oldConfig.get(oldKey));
			oldConfig.set(oldKey, null);
			return true;
		}
		return false;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	@Override
	public @NotNull Quests getQuestsConfig() {
		return quests;
	}

	@Override
	public @NotNull DialogsConfig getDialogsConfig() {
		return dialogs;
	}

	@Override
	public @NotNull QuestsMenuConfig getQuestsMenuConfig() {
		return menu;
	}

	@Override
	public @NotNull StageDescriptionConfig getStageDescriptionConfig() {
		return stageDescription;
	}

	@Override
	public @NotNull QuestDescriptionConfig getQuestDescriptionConfig() {
		return questDescription;
	}

	public String getPrefix() {
		return (enablePrefix) ? Lang.Prefix.toString() : "§6";
	}

	public boolean isTextHologramDisabled() {
		return disableTextHologram;
	}

	public boolean showStartParticles() {
		return particleStart != null;
	}

	public ParticleEffect getParticleStart() {
		return particleStart;
	}

	public boolean showTalkParticles() {
		return particleTalk != null;
	}

	public ParticleEffect getParticleTalk() {
		return particleTalk;
	}

	public boolean showNextParticles() {
		return particleNext != null;
	}

	public ParticleEffect getParticleNext() {
		return particleNext;
	}

	public double getHologramsHeight() {
		return hologramsHeight;
	}

	public boolean isCustomHologramNameShown() {
		return showCustomHologramName;
	}

	public ItemStack getHoloLaunchItem() {
		return holoLaunchItem;
	}

	public ItemStack getHoloLaunchNoItem() {
		return holoLaunchNoItem;
	}

	public ItemStack getHoloTalkItem() {
		return holoTalkItem;
	}

	public FireworkMeta getDefaultFirework() {
		return defaultFirework;
	}

	public boolean hookAccounts() {
		return hookAcounts && InternalIntegrations.AccountsHook.isEnabled();
	}

	public boolean usePlayerBlockTracker() {
		return usePlayerBlockTracker && InternalIntegrations.PlayerBlockTracker.isEnabled();
	}

	public boolean isMinecraftTranslationsEnabled() {
		return minecraftTranslationsFile != null && !minecraftTranslationsFile.isEmpty();
	}

	public QuestDescription getQuestDescription() {
		return questDescription;
	}

	public class QuestsConfig implements QuestsConfiguration.Quests {

		private int defaultTimer = 5;
		private int maxLaunchedQuests = 0;
		private boolean scoreboards = true;
		private boolean sounds = true;
		private boolean fireworks = true;
		private String finishSound = "ENTITY_PLAYER_LEVELUP";
		private String nextStageSound = "ITEM_FIRECHARGE_USE";
		private ItemStack defaultQuestItem = XMaterial.BOOK.parseItem();
		private XMaterial pageItem = XMaterial.ARROW;
		private int startParticleDistance;
		private int requirementUpdateTime;
		private boolean sendUpdate = true;
		private boolean stageStart = true;
		private boolean questConfirmGUI = false;
		private Collection<NpcClickType> npcClicks = Arrays.asList(NpcClickType.RIGHT, NpcClickType.SHIFT_RIGHT);
		private boolean skipNpcGuiIfOnlyOneQuest = true;
		private boolean requirementReasonOnMultipleQuests = true;
		private boolean stageEndRewardsMessage = true;

		private void init() {
			defaultTimer = config.getInt("redoMinuts");
			maxLaunchedQuests = config.getInt("maxLaunchedQuests");
			scoreboards = config.getBoolean("scoreboards");
			sendUpdate = config.getBoolean("playerQuestUpdateMessage");
			stageStart = config.getBoolean("playerStageStartMessage");
			questConfirmGUI = config.getBoolean("questConfirmGUI");
			sounds = config.getBoolean("sounds");
			fireworks = config.getBoolean("fireworks");
			if (config.contains("pageItem"))
				pageItem = XMaterial.matchXMaterial(config.getString("pageItem")).orElse(XMaterial.ARROW);
			if (pageItem == null)
				pageItem = XMaterial.ARROW;
			if (config.isItemStack("item")) {
				defaultQuestItem = config.getItemStack("item");
			} else if (config.isString("item")) {
				defaultQuestItem = XMaterial.matchXMaterial(config.getString("item")).orElse(XMaterial.BOOK).parseItem();
			} else
				defaultQuestItem = XMaterial.BOOK.parseItem();
			defaultQuestItem = ItemUtils.clearVisibleAttributes(defaultQuestItem);
			startParticleDistance = config.getInt("startParticleDistance");
			requirementUpdateTime = config.getInt("requirementUpdateTime");
			finishSound = loadSound("finishSound");
			nextStageSound = loadSound("nextStageSound");
			try {
				if (config.isString("npcClick")) {
					String click = config.getString("npcClick");
					npcClicks = Arrays.asList(click.equals("ANY") ? NpcClickType.values()
							: new NpcClickType[] {NpcClickType.valueOf(click.toUpperCase())});
				} else {
					npcClicks = config.getStringList("npcClick")
							.stream()
							.map(String::toUpperCase)
							.map(NpcClickType::valueOf)
							.collect(Collectors.toList());
				}
			} catch (IllegalArgumentException ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.warning("Unknown click type " + config.get("npcClick") + " for config entry \"npcClick\"");
			}
			skipNpcGuiIfOnlyOneQuest = config.getBoolean("skip npc gui if only one quest");
			requirementReasonOnMultipleQuests = config.getBoolean("requirementReasonOnMultipleQuests");
			stageEndRewardsMessage = config.getBoolean("stageEndRewardsMessage");
		}

		@Override
		public int getDefaultTimer() {
			return defaultTimer;
		}

		@Override
		public int maxLaunchedQuests() {
			return maxLaunchedQuests;
		}

		@Override
		public boolean scoreboards() {
			return scoreboards;
		}

		@Override
		public boolean playerQuestUpdateMessage() {
			return sendUpdate;
		}

		@Override
		public boolean playerStageStartMessage() {
			return stageStart;
		}

		@Override
		public boolean questConfirmGUI() {
			return questConfirmGUI;
		}

		@Override
		public boolean sounds() {
			return sounds;
		}

		@Override
		public String finishSound() {
			return finishSound;
		}

		@Override
		public String nextStageSound() {
			return nextStageSound;
		}

		@Override
		public boolean fireworks() {
			return fireworks;
		}

		@Override
		public Collection<NpcClickType> getNpcClicks() {
			return npcClicks;
		}

		@Override
		public boolean skipNpcGuiIfOnlyOneQuest() {
			return skipNpcGuiIfOnlyOneQuest;
		}

		@Override
		public ItemStack getDefaultQuestItem() {
			return defaultQuestItem;
		}

		@Override
		public XMaterial getPageMaterial() {
			return pageItem;
		}

		@Override
		public double startParticleDistance() {
			return startParticleDistance;
		}

		@Override
		public int requirementUpdateTime() {
			return requirementUpdateTime;
		}

		@Override
		public boolean requirementReasonOnMultipleQuests() {
			return requirementReasonOnMultipleQuests;
		}

		@Override
		public boolean stageEndRewardsMessage() {
			return stageEndRewardsMessage;
		}

	}

	public class DialogsConfig implements QuestsConfiguration.Dialogs {

		private boolean inActionBar = false;
		private int defaultTime = 100;
		private boolean defaultSkippable = true;
		private boolean disableClick = false;
		private boolean history = true;
		private int maxMessagesPerHistoryPage = -1;
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
				result |= migrateEntry(config.getParent(), "dialogsInActionBar", config, "inActionBar");
				result |= migrateEntry(config.getParent(), "dialogsDefaultTime", config, "defaultTime");
				result |= migrateEntry(config.getParent(), "disableDialogClick", config, "disableClick");
				result |= migrateEntry(config.getParent(), "dialogHistory", config, "history");
			}
			return result;
		}

		private void init() {
			inActionBar = MinecraftVersion.MAJOR > 8 && config.getBoolean("inActionBar");
			defaultTime = config.getInt("defaultTime");
			defaultSkippable = config.getBoolean("defaultSkippable");
			disableClick = config.getBoolean("disableClick");
			history = config.getBoolean("history");
			maxMessagesPerHistoryPage = config.getInt("max messages per history page");
			maxDistance = config.getInt("maxDistance");
			maxDistanceSquared = maxDistance <= 0 ? 0 : (maxDistance * maxDistance);

			defaultPlayerSound = config.getString("defaultPlayerSound");
			defaultNPCSound = config.getString("defaultNPCSound");
		}

		@Override
		public boolean sendInActionBar() {
			return inActionBar;
		}

		@Override
		public int getDefaultTime() {
			return defaultTime;
		}

		@Override
		public boolean isSkippableByDefault() {
			return defaultSkippable;
		}

		@Override
		public boolean isClickDisabled() {
			return disableClick;
		}

		@Override
		public boolean isHistoryEnabled() {
			return history;
		}

		@Override
		public int getMaxMessagesPerHistoryPage() {
			return maxMessagesPerHistoryPage;
		}

		@Override
		public int getMaxDistance() {
			return maxDistance;
		}

		@Override
		public int getMaxDistanceSquared() {
			return maxDistanceSquared;
		}

		@Override
		public String getDefaultPlayerSound() {
			return defaultPlayerSound;
		}

		@Override
		public String getDefaultNPCSound() {
			return defaultNPCSound;
		}

	}

	public class QuestsMenuConfig implements QuestsConfiguration.QuestsMenu {

		private Set<PlayerListCategory> tabs;
		private boolean openNotStartedTabWhenEmpty = true;
		private boolean allowPlayerCancelQuest = true;

		private ConfigurationSection config;

		private QuestsMenuConfig(ConfigurationSection config) {
			this.config = config;
		}

		private boolean update() {
			boolean result = false;
			if (config.getParent() != null) {
				result |= migrateEntry(config.getParent(), "menuOpenNotStartedTabWhenEmpty", config, "openNotStartedTabWhenEmpty");
				result |= migrateEntry(config.getParent(), "allowPlayerCancelQuest", config, "allowPlayerCancelQuest");
			}
			return result;
		}

		private void init() {
			tabs = config.getStringList("enabledTabs").stream().map(PlayerListCategory::fromString).collect(Collectors.toSet());
			if (tabs.isEmpty()) {
				QuestsPlugin.getPlugin().getLoggerExpanded().warning("Quests Menu must have at least one enabled tab.");
				tabs = EnumSet.allOf(PlayerListCategory.class);
			}
			openNotStartedTabWhenEmpty = config.getBoolean("openNotStartedTabWhenEmpty");
			allowPlayerCancelQuest = config.getBoolean("allowPlayerCancelQuest");
		}

		@Override
		public boolean isNotStartedTabOpenedWhenEmpty() {
			return openNotStartedTabWhenEmpty;
		}

		@Override
		public boolean allowPlayerCancelQuest() {
			return allowPlayerCancelQuest;
		}

		@Override
		public Set<PlayerListCategory> getEnabledTabs() {
			return tabs;
		}

	}

	public class StageDescriptionConfig implements QuestsConfiguration.StageDescription {

		private String itemSingleFormat, itemMultipleFormat;
		private String stageDescriptionFormat;
		private String descPrefix = "{nl}§e- §6";
		private boolean inlineAlone = true;
		private Set<DescriptionSource> descSources = EnumSet.noneOf(DescriptionSource.class);
		private boolean bossBars = true;
		private String bossBarFormat;
		private int bossBarTimeout = 15;

		private final ConfigurationSection config;

		public StageDescriptionConfig(ConfigurationSection config) {
			this.config = config;
		}

		private void init() {
			itemSingleFormat = config.getString("item formats.single");
			itemMultipleFormat = config.getString("item formats.multiple");
			stageDescriptionFormat = config.getString("description format");
			descPrefix = "{nl}" + config.getString("line prefix");
			inlineAlone = config.getBoolean("inline alone");
			for (String s : config.getStringList("split sources")) {
				try {
					descSources.add(DescriptionSource.valueOf(s));
				} catch (IllegalArgumentException ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded()
							.warning("Loading of description splitted sources failed : source " + s + " does not exist");
				}
			}
			bossBars = config.getBoolean("boss bars");
			bossBarFormat = config.getString("boss bar format");
			bossBarTimeout = config.getInt("boss bar timeout");
		}

		private boolean update() {
			boolean result = false;

			ConfigurationSection oldConfig = config.getParent().getConfigurationSection("stageDescriptionItemsSplit");

			result |= migrateEntry(config.getParent(), "stageDescriptionFormat", config, "description format");
			if (oldConfig != null) {
				migrateEntry(oldConfig, "prefix", config, "line prefix");
				migrateEntry(oldConfig, "sources", config, "split sources");
				migrateEntry(oldConfig, "inlineAlone", config, "inline alone");

				String amountFormat = oldConfig.getString("amountFormat");
				boolean showXOne = oldConfig.getBoolean("showXOne");
				String itemNameColor = config.getParent().getString("itemNameColor");
				String itemAmountColor = config.getParent().getString("itemAmountColor");

				String multipleFormat = itemNameColor + "{name}" + itemAmountColor + " " + amountFormat;
				String singleFormat = showXOne ? multipleFormat : (itemNameColor + "{name}");
				config.set("item formats.multiple", multipleFormat);
				config.set("item formats.single", singleFormat);

				config.getParent().set("itemNameColor", null);
				config.getParent().set("itemAmountColor", null);
				config.getParent().set("stageDescriptionItemsSplit", null);

				result = true;
			}

			result |= migrateEntry(config.getParent(), "mobsProgressBar", config, "boss bars");
			result |= migrateEntry(config.getParent(), "progressBarTimeoutSeconds", config, "boss bar timeout");

			return result;
		}

		@Override
		public String getStageDescriptionFormat() {
			return stageDescriptionFormat;
		}

		@Override
		public @NotNull String getSingleItemFormat() {
			return itemSingleFormat;
		}

		@Override
		public @NotNull String getMultipleItemsFormat() {
			return itemMultipleFormat;
		}

		@Override
		public String getSplitPrefix() {
			return descPrefix;
		}

		@Override
		public boolean isAloneSplitInlined() {
			return inlineAlone;
		}

		@Override
		public Set<DescriptionSource> getSplitSources() {
			return descSources;
		}

		@Override
		public boolean areBossBarsEnabled() {
			return bossBars;
		}

		@Override
		public String getBossBarFormat() {
			return bossBarFormat;
		}

		@Override
		public int getBossBarTimeout() {
			return bossBarTimeout;
		}

	}

	public class QuestDescriptionConfig implements QuestDescription {

		private boolean requirements;
		private String requirementsValid;
		private String requirementsInvalid;

		private boolean rewards;
		private String rewardsFormat;

		private final ConfigurationSection config;

		public QuestDescriptionConfig(ConfigurationSection config) {
			this.config = config;
		}

		private void init() {
			requirements = config.getBoolean("requirements.display");
			requirementsValid = config.getString("requirements.valid");
			requirementsInvalid = config.getString("requirements.invalid");

			rewards = config.getBoolean("rewards.display");
			rewardsFormat = config.getString("rewards.format");
		}

		@Override
		public boolean showRewards() {
			return rewards;
		}

		@Override
		public String getRewardsFormat() {
			return rewardsFormat;
		}

		@Override
		public boolean showRequirements() {
			return requirements;
		}

		@Override
		public String getRequirementsValid() {
			return requirementsValid;
		}

		@Override
		public String getRequirementsInvalid() {
			return requirementsInvalid;
		}

	}

}
