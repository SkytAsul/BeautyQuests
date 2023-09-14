package fr.skytasul.quests;

import static fr.skytasul.quests.api.gui.ItemUtils.item;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.requirements.RequirementList;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.rewards.RewardList;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.stages.StageTypeRegistry;
import fr.skytasul.quests.api.stages.options.StageOptionAutoRegister;
import fr.skytasul.quests.api.stages.options.StageOptionCreator;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.api.utils.XMaterial;
import fr.skytasul.quests.api.utils.messaging.MessageProcessor;
import fr.skytasul.quests.api.utils.messaging.MessageType;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.api.utils.messaging.PlaceholdersContext;
import fr.skytasul.quests.api.utils.progress.HasProgress;
import fr.skytasul.quests.mobs.BukkitEntityFactory;
import fr.skytasul.quests.options.*;
import fr.skytasul.quests.requirements.*;
import fr.skytasul.quests.requirements.logical.LogicalOrRequirement;
import fr.skytasul.quests.rewards.*;
import fr.skytasul.quests.stages.*;
import fr.skytasul.quests.stages.options.StageOptionProgressBar;
import fr.skytasul.quests.utils.QuestUtils;
import fr.skytasul.quests.utils.compatibility.BQBossBarImplementation;
import net.md_5.bungee.api.ChatColor;

public final class DefaultQuestFeatures {

	private static boolean npcFeaturesRegistered = false;

	private DefaultQuestFeatures() {}

	public static void registerStages() {
		StageTypeRegistry stages = QuestsAPI.getAPI().getStages();
		stages.register(new StageType<>("MOBS", StageMobs.class, Lang.Mobs.name(),
				StageMobs::deserialize, item(XMaterial.WOODEN_SWORD, Lang.stageMobs.toString()),
				StageMobs.Creator::new));
		stages.register(new StageType<>("MINE", StageMine.class, Lang.Mine.name(),
				StageMine::deserialize, item(XMaterial.WOODEN_PICKAXE, Lang.stageMine.toString()),
				StageMine.Creator::new));
		stages.register(new StageType<>("PLACE_BLOCKS", StagePlaceBlocks.class, Lang.Place.name(),
				StagePlaceBlocks::deserialize, item(XMaterial.OAK_STAIRS, Lang.stagePlace.toString()),
				StagePlaceBlocks.Creator::new));
		stages.register(new StageType<>("CHAT", StageChat.class, Lang.Chat.name(),
				StageChat::deserialize, item(XMaterial.PLAYER_HEAD, Lang.stageChat.toString()),
				StageChat.Creator::new));
		stages.register(new StageType<>("INTERACT_BLOCK", StageInteractBlock.class, Lang.InteractBlock.name(),
				StageInteractBlock::deserialize, item(XMaterial.STICK, Lang.stageInteractBlock.toString()),
				StageInteractBlock.Creator::new));
		stages.register(new StageType<>("INTERACT_LOCATION", StageInteractLocation.class, Lang.InteractLocation.name(),
				StageInteractLocation::deserialize, item(XMaterial.BEACON, Lang.stageInteractLocation.toString()),
				StageInteractLocation.Creator::new));
		stages.register(new StageType<>("FISH", StageFish.class, Lang.Fish.name(),
				StageFish::deserialize, item(XMaterial.COD, Lang.stageFish.toString()), StageFish.Creator::new));
		stages.register(new StageType<>("MELT", StageMelt.class, Lang.Melt.name(),
				StageMelt::deserialize, item(XMaterial.FURNACE, Lang.stageMelt.toString()),
				StageMelt.Creator::new));
		stages.register(new StageType<>("ENCHANT", StageEnchant.class, Lang.Enchant.name(),
				StageEnchant::deserialize, item(XMaterial.ENCHANTING_TABLE, Lang.stageEnchant.toString()),
				StageEnchant.Creator::new));
		stages.register(new StageType<>("CRAFT", StageCraft.class, Lang.Craft.name(),
				StageCraft::deserialize, item(XMaterial.CRAFTING_TABLE, Lang.stageCraft.toString()),
				StageCraft.Creator::new));
		stages.register(new StageType<>("BUCKET", StageBucket.class, Lang.Bucket.name(),
				StageBucket::deserialize, item(XMaterial.BUCKET, Lang.stageBucket.toString()),
				StageBucket.Creator::new));
		stages.register(new StageType<>("LOCATION", StageLocation.class, Lang.StageLocation.name(),
				StageLocation::deserialize, item(XMaterial.MINECART, Lang.stageLocation.toString()),
				StageLocation.Creator::new));
		stages.register(new StageType<>("PLAY_TIME", StagePlayTime.class, Lang.PlayTime.name(),
				StagePlayTime::deserialize, item(XMaterial.CLOCK, Lang.stagePlayTime.toString()),
				StagePlayTime.Creator::new));
		stages.register(new StageType<>("BREED", StageBreed.class, Lang.Breed.name(),
				StageBreed::deserialize, item(XMaterial.WHEAT, Lang.stageBreedAnimals.toString()),
				StageBreed.Creator::new));
		stages.register(new StageType<>("TAME", StageTame.class, Lang.Tame.name(),
				StageTame::deserialize, item(XMaterial.CARROT, Lang.stageTameAnimals.toString()),
				StageTame.Creator::new));
		stages.register(new StageType<>("DEATH", StageDeath.class, Lang.Death.name(),
				StageDeath::deserialize, item(XMaterial.SKELETON_SKULL, Lang.stageDeath.toString()),
				StageDeath.Creator::new));
		stages.register(new StageType<>("DEAL_DAMAGE", StageDealDamage.class, Lang.DealDamage.name(),
				StageDealDamage::deserialize, item(XMaterial.REDSTONE, Lang.stageDealDamage.toString()),
				StageDealDamage.Creator::new));
		stages.register(new StageType<>("EAT_DRINK", StageEatDrink.class, Lang.EatDrink.name(),
				StageEatDrink::new, item(XMaterial.COOKED_PORKCHOP, Lang.stageEatDrink.toString()),
				StageEatDrink.Creator::new));
	}

	public static void registerStageOptions() {
		QuestsAPI.getAPI().getStages().autoRegisterOption(new StageOptionAutoRegister() {

			@Override
			public boolean appliesTo(@NotNull StageType<?> type) {
				return HasProgress.class.isAssignableFrom(type.getStageClass());
			}

			@SuppressWarnings("rawtypes")
			@Override
			public <T extends AbstractStage> StageOptionCreator<T> createOptionCreator(@NotNull StageType<T> type) {
				return createOptionCreatorInternal((@NotNull StageType) type); // NOSONAR magic
			}

			private <T extends AbstractStage & HasProgress> StageOptionCreator<T> createOptionCreatorInternal(
					@NotNull StageType<T> type) {
				return StageOptionCreator.create("progressbar", StageOptionProgressBar.class,
						() -> new StageOptionProgressBar<>(type.getStageClass()));
			}

		});
	}

	public static void registerQuestOptions() {
		QuestsAPI.getAPI()
				.registerQuestOption(new QuestOptionCreator<>("pool", 9, OptionQuestPool.class, OptionQuestPool::new, null));
		QuestsAPI.getAPI()
				.registerQuestOption(new QuestOptionCreator<>("name", 10, OptionName.class, OptionName::new, null));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("description", 12, OptionDescription.class, OptionDescription::new, null));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("customItem", 13, OptionQuestItem.class,
				OptionQuestItem::new, QuestsConfiguration.getConfig().getQuestsConfig().getDefaultQuestItem(),
				"customMaterial"));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("confirmMessage", 15, OptionConfirmMessage.class, OptionConfirmMessage::new, null));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("bypassLimit", 18, OptionBypassLimit.class, OptionBypassLimit::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("startableFromGUI", 19, OptionStartable.class, OptionStartable::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("failOnDeath", 20, OptionFailOnDeath.class, OptionFailOnDeath::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("cancellable", 21, OptionCancellable.class, OptionCancellable::new, true));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("cancelActions", 22, OptionCancelRewards.class,
				OptionCancelRewards::new, new RewardList()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("scoreboard", 27, OptionScoreboardEnabled.class,
				OptionScoreboardEnabled::new, true));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hideNoRequirements", 28,
				OptionHideNoRequirements.class, OptionHideNoRequirements::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("auto", 29, OptionAutoQuest.class, OptionAutoQuest::new, false));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("repeatable", 30, OptionRepeatable.class,
				OptionRepeatable::new, false, "multiple"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("timer", 31, OptionTimer.class, OptionTimer::new,
				QuestsConfiguration.getConfig().getQuestsConfig().getDefaultTimer()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("visibility", 32, OptionVisibility.class,
				OptionVisibility::new, Arrays.asList(QuestVisibilityLocation.values()), "hid", "hide"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("endSound", 34, OptionEndSound.class,
				OptionEndSound::new, QuestsConfiguration.getConfig().getQuestsConfig().finishSound()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("firework", 35, OptionFirework.class,
				OptionFirework::new, QuestsConfigurationImplementation.getConfiguration().getDefaultFirework()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("requirements", 36, OptionRequirements.class,
				OptionRequirements::new, new RequirementList()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("startRewards", 38, OptionStartRewards.class,
				OptionStartRewards::new, new RewardList(), "startRewardsList"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("startMessage", 39, OptionStartMessage.class,
				OptionStartMessage::new,
				QuestsConfigurationImplementation.getConfiguration().getPrefix() + Lang.STARTED_QUEST.toString()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("endRewards", 43, OptionEndRewards.class,
				OptionEndRewards::new, new RewardList(), "rewardsList"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("endMsg", 44, OptionEndMessage.class,
				OptionEndMessage::new,
				QuestsConfigurationImplementation.getConfiguration().getPrefix() + Lang.FINISHED_BASE.toString()));
	}

	public static void registerRewards() {
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("commandReward", CommandReward.class,
				item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("itemReward", ItemReward.class,
				item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), ItemReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("removeItemsReward", RemoveItemsReward.class,
				item(XMaterial.CHEST, Lang.rewardRemoveItems.toString()), RemoveItemsReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("textReward", MessageReward.class,
				item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), MessageReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("tpReward", TeleportationReward.class,
				item(XMaterial.ENDER_PEARL, Lang.location.toString()), TeleportationReward::new, false));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("expReward", XPReward.class,
				item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), XPReward::new));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("checkpointReward", CheckpointReward.class,
						item(XMaterial.NETHER_STAR, Lang.rewardCheckpoint.toString()), CheckpointReward::new,
						false, QuestObjectLocation.STAGE));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("questStopReward", QuestStopReward.class,
						item(XMaterial.BARRIER, Lang.rewardStopQuest.toString()), QuestStopReward::new, false,
						QuestObjectLocation.STAGE));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("requirementDependentReward", RequirementDependentReward.class,
						item(XMaterial.REDSTONE, Lang.rewardWithRequirements.toString()),
						RequirementDependentReward::new, true).setCanBeAsync(true));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("randomReward", RandomReward.class,
						item(XMaterial.EMERALD, Lang.rewardRandom.toString()), RandomReward::new, true)
								.setCanBeAsync(true));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("wait", WaitReward.class,
						item(XMaterial.CLOCK, Lang.rewardWait.toString()), WaitReward::new, true)
								.setCanBeAsync(true));
		if (MinecraftVersion.MAJOR >= 9)
			QuestsAPI.getAPI().getRewards().register(new RewardCreator("titleReward", TitleReward.class,
					item(XMaterial.NAME_TAG, Lang.rewardTitle.toString()), TitleReward::new, false));
	}

	public static void registerRequirements() {
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("logicalOr", LogicalOrRequirement.class,
				item(XMaterial.REDSTONE_TORCH, Lang.RLOR.toString()), LogicalOrRequirement::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("questRequired", QuestRequirement.class,
				item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), QuestRequirement::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("levelRequired", LevelRequirement.class,
				item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), LevelRequirement::new));
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("permissionRequired", PermissionsRequirement.class,
						item(XMaterial.PAPER, Lang.RPermissions.toString()), PermissionsRequirement::new));
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("scoreboardRequired", ScoreboardRequirement.class,
						item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), ScoreboardRequirement::new));
		if (MinecraftVersion.MAJOR >= 9)
			QuestsAPI.getAPI().getRequirements()
					.register(new RequirementCreator("equipmentRequired", EquipmentRequirement.class,
							item(XMaterial.CHAINMAIL_HELMET, Lang.REquipment.toString()),
							EquipmentRequirement::new));
	}

	public static void registerItemComparisons() {
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("bukkit", Lang.comparisonBukkit.toString(),
				Lang.comparisonBukkitLore.toString(), ItemStack::isSimilar).setEnabledByDefault());
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("customBukkit", Lang.comparisonCustomBukkit.toString(),
				Lang.comparisonCustomBukkitLore.toString(), QuestUtils::isSimilar));
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("material", Lang.comparisonMaterial.toString(),
				Lang.comparisonMaterialLore.toString(), (item1, item2) -> {
					if (item2.getType() != item1.getType())
						return false;
					if (item1.getType().getMaxDurability() > 0 || MinecraftVersion.MAJOR >= 13)
						return true;
					return item2.getDurability() == item1.getDurability();
				}));
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("name", Lang.comparisonName.toString(),
				Lang.comparisonNameLore.toString(), (item1, item2) -> {
					ItemMeta meta1 = item1.getItemMeta();
					ItemMeta meta2 = item2.getItemMeta();
					return (meta1.hasDisplayName() == meta2.hasDisplayName())
							&& Objects.equals(meta1.getDisplayName(), meta2.getDisplayName());
				}).setMetaNeeded());
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("lore", Lang.comparisonLore.toString(),
				Lang.comparisonLoreLore.toString(), (item1, item2) -> {
					ItemMeta meta1 = item1.getItemMeta();
					ItemMeta meta2 = item2.getItemMeta();
					return (meta1.hasLore() == meta2.hasLore()) && Objects.equals(meta1.getLore(), meta2.getLore());
				}).setMetaNeeded());
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("enchants", Lang.comparisonEnchants.toString(),
				Lang.comparisonEnchantsLore.toString(), (item1, item2) -> {
					ItemMeta meta1 = item1.getItemMeta();
					ItemMeta meta2 = item2.getItemMeta();
					return (meta1.hasEnchants() == meta2.hasEnchants())
							&& Objects.equals(meta1.getEnchants(), meta2.getEnchants());
				}).setMetaNeeded());
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("repair", Lang.comparisonRepairCost.toString(),
				Lang.comparisonRepairCostLore.toString(), (item1, item2) -> {
					ItemMeta meta1 = item1.getItemMeta();
					if (!(meta1 instanceof Repairable))
						return true;
					ItemMeta meta2 = item2.getItemMeta();
					if (!(meta2 instanceof Repairable))
						return true;
					return ((Repairable) meta1).getRepairCost() == ((Repairable) meta2).getRepairCost();
				}).setMetaNeeded());
	}

	public static void registerMisc() {
		QuestsAPI.getAPI().registerMobFactory(new BukkitEntityFactory());
		if (MinecraftVersion.MAJOR >= 9)
			QuestsAPI.getAPI().setBossBarManager(new BQBossBarImplementation());
	}

	public static void registerMessageProcessors() {
		PlaceholderRegistry defaultPlaceholders = new PlaceholderRegistry()
				.registerContextual("player", PlaceholdersContext.class, context -> context.getActor().getName())
				.registerContextual("PLAYER", PlaceholdersContext.class, context -> context.getActor().getName())
				.register("prefix", () -> BeautyQuests.getInstance().getPrefix())
				.register("nl", "\n");

		QuestsAPI.getAPI().registerMessageProcessor("default_message_type", 1, new MessageProcessor() {
			@Override
			public String processString(String string, PlaceholdersContext context) {
				if (context.getMessageType() == MessageType.DefaultMessageType.PREFIXED)
					return BeautyQuests.getInstance().getPrefix() + string;
				if (context.getMessageType() == MessageType.DefaultMessageType.UNPREFIXED)
					return "§6" + string;
				if (context.getMessageType() == MessageType.DefaultMessageType.OFF)
					return Lang.OffText.quickFormat("message", string);
				return string;
			}
		});

		QuestsAPI.getAPI().registerMessageProcessor("default_placeholders", 2, new MessageProcessor() {
			@Override
			public PlaceholderRegistry processPlaceholders(PlaceholderRegistry placeholders, PlaceholdersContext context) {
				if (context.replacePluginPlaceholders())
					return placeholders == null ? defaultPlaceholders : placeholders.with(defaultPlaceholders);
				else
					return placeholders;
			}
		});

		QuestsAPI.getAPI().registerMessageProcessor("legacy_colors", 10, new MessageProcessor() {
			@Override
			public String processString(String string, PlaceholdersContext context) {
				return ChatColor.translateAlternateColorCodes('&', string);
			}
		});
	}

	public static void registerNpcFeatures() {
		if (npcFeaturesRegistered)
			return;
		npcFeaturesRegistered = true;

		StageTypeRegistry stages = QuestsAPI.getAPI().getStages();
		stages.register(new StageType<>("NPC", StageNPC.class, Lang.Talk.name(),
				StageNPC::deserialize, item(XMaterial.OAK_SIGN, Lang.stageNPC.toString()), StageNPC.Creator::new));
		stages.register(new StageType<>("ITEMS", StageBringBack.class, Lang.Items.name(),
				StageBringBack::deserialize, item(XMaterial.CHEST, Lang.stageBring.toString()),
				StageBringBack.Creator::new));

		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("starterNPC", 40, OptionStarterNPC.class,
				OptionStarterNPC::new, null, "starterID"));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("startDialog", 41, OptionStartDialog.class, OptionStartDialog::new, null));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hologramText", 17, OptionHologramText.class,
				OptionHologramText::new, Lang.HologramText.toString()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hologramLaunch", 25, OptionHologramLaunch.class,
				OptionHologramLaunch::new, QuestsConfigurationImplementation.getConfiguration().getHoloLaunchItem()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hologramLaunchNo", 26, OptionHologramLaunchNo.class,
				OptionHologramLaunchNo::new, QuestsConfigurationImplementation.getConfiguration().getHoloLaunchNoItem()));
	}

}
