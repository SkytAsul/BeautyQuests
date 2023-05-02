package fr.skytasul.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import com.cryptomorin.xseries.XMaterial;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.comparison.ItemComparison;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.objects.QuestObjectLocation;
import fr.skytasul.quests.api.options.QuestOptionCreator;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.rewards.RewardCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.utils.MinecraftVersion;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.api.utils.QuestVisibilityLocation;
import fr.skytasul.quests.options.*;
import fr.skytasul.quests.requirements.EquipmentRequirement;
import fr.skytasul.quests.requirements.LevelRequirement;
import fr.skytasul.quests.requirements.PermissionsRequirement;
import fr.skytasul.quests.requirements.QuestRequirement;
import fr.skytasul.quests.requirements.ScoreboardRequirement;
import fr.skytasul.quests.requirements.logical.LogicalOrRequirement;
import fr.skytasul.quests.rewards.*;
import fr.skytasul.quests.stages.*;

public final class DefaultQuestFeatures {

	private DefaultQuestFeatures() {}

	private static final ItemStack stageNPC = ItemUtils.item(XMaterial.OAK_SIGN, Lang.stageNPC.toString());
	private static final ItemStack stageItems = ItemUtils.item(XMaterial.CHEST, Lang.stageBring.toString());
	private static final ItemStack stageMobs = ItemUtils.item(XMaterial.WOODEN_SWORD, Lang.stageMobs.toString());
	private static final ItemStack stageMine = ItemUtils.item(XMaterial.WOODEN_PICKAXE, Lang.stageMine.toString());
	private static final ItemStack stagePlace = ItemUtils.item(XMaterial.OAK_STAIRS, Lang.stagePlace.toString());
	private static final ItemStack stageChat = ItemUtils.item(XMaterial.PLAYER_HEAD, Lang.stageChat.toString());
	private static final ItemStack stageInteract = ItemUtils.item(XMaterial.STICK, Lang.stageInteract.toString());
	private static final ItemStack stageFish = ItemUtils.item(XMaterial.COD, Lang.stageFish.toString());
	private static final ItemStack stageMelt = ItemUtils.item(XMaterial.FURNACE, Lang.stageMelt.toString());
	private static final ItemStack stageEnchant = ItemUtils.item(XMaterial.ENCHANTING_TABLE, Lang.stageEnchant.toString());
	private static final ItemStack stageCraft = ItemUtils.item(XMaterial.CRAFTING_TABLE, Lang.stageCraft.toString());
	private static final ItemStack stageBucket = ItemUtils.item(XMaterial.BUCKET, Lang.stageBucket.toString());
	private static final ItemStack stageLocation = ItemUtils.item(XMaterial.MINECART, Lang.stageLocation.toString());
	private static final ItemStack stagePlayTime = ItemUtils.item(XMaterial.CLOCK, Lang.stagePlayTime.toString());
	private static final ItemStack stageBreed = ItemUtils.item(XMaterial.WHEAT, Lang.stageBreedAnimals.toString());
	private static final ItemStack stageTame = ItemUtils.item(XMaterial.CARROT, Lang.stageTameAnimals.toString());
	private static final ItemStack stageDeath = ItemUtils.item(XMaterial.SKELETON_SKULL, Lang.stageDeath.toString());
	private static final ItemStack stageDealDamage = ItemUtils.item(XMaterial.REDSTONE, Lang.stageDealDamage.toString());
	private static final ItemStack stageEatDrink = ItemUtils.item(XMaterial.COOKED_PORKCHOP, Lang.stageEatDrink.toString());

	public static void registerStages() {
		QuestsAPI.getAPI().getStages().register(new StageType<>("NPC", StageNPC.class, Lang.Talk.name(),
				StageNPC::deserialize, stageNPC, StageNPC.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("ITEMS", StageBringBack.class, Lang.Items.name(),
				StageBringBack::deserialize, stageItems, StageBringBack.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("MOBS", StageMobs.class, Lang.Mobs.name(),
				StageMobs::deserialize, stageMobs, StageMobs.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("MINE", StageMine.class, Lang.Mine.name(),
				StageMine::deserialize, stageMine, StageMine.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("PLACE_BLOCKS", StagePlaceBlocks.class, Lang.Place.name(),
				StagePlaceBlocks::deserialize, stagePlace, StagePlaceBlocks.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("CHAT", StageChat.class, Lang.Chat.name(),
				StageChat::deserialize, stageChat, StageChat.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("INTERACT", StageInteract.class, Lang.Interact.name(),
				StageInteract::deserialize, stageInteract, StageInteract.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("FISH", StageFish.class, Lang.Fish.name(),
				StageFish::deserialize, stageFish, StageFish.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("MELT", StageMelt.class, Lang.Melt.name(),
				StageMelt::deserialize, stageMelt, StageMelt.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("ENCHANT", StageEnchant.class, Lang.Enchant.name(),
				StageEnchant::deserialize, stageEnchant, StageEnchant.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("CRAFT", StageCraft.class, Lang.Craft.name(),
				StageCraft::deserialize, stageCraft, StageCraft.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("BUCKET", StageBucket.class, Lang.Bucket.name(),
				StageBucket::deserialize, stageBucket, StageBucket.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("LOCATION", StageLocation.class, Lang.Location.name(),
				StageLocation::deserialize, stageLocation, StageLocation.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("PLAY_TIME", StagePlayTime.class, Lang.PlayTime.name(),
				StagePlayTime::deserialize, stagePlayTime, StagePlayTime.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("BREED", StageBreed.class, Lang.Breed.name(),
				StageBreed::deserialize, stageBreed, StageBreed.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("TAME", StageTame.class, Lang.Tame.name(),
				StageTame::deserialize, stageTame, StageTame.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("DEATH", StageDeath.class, Lang.Death.name(),
				StageDeath::deserialize, stageDeath, StageDeath.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("DEAL_DAMAGE", StageDealDamage.class, Lang.DealDamage.name(),
				StageDealDamage::deserialize, stageDealDamage, StageDealDamage.Creator::new));
		QuestsAPI.getAPI().getStages().register(new StageType<>("EAT_DRINK", StageEatDrink.class, Lang.EatDrink.name(),
				StageEatDrink::new, stageEatDrink, StageEatDrink.Creator::new));
	}

	public static void registerQuestOptions() {
		QuestsAPI.getAPI()
				.registerQuestOption(new QuestOptionCreator<>("pool", 9, OptionQuestPool.class, OptionQuestPool::new, null));
		QuestsAPI.getAPI()
				.registerQuestOption(new QuestOptionCreator<>("name", 10, OptionName.class, OptionName::new, null));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("description", 12, OptionDescription.class, OptionDescription::new, null));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("customItem", 13, OptionQuestItem.class,
				OptionQuestItem::new, QuestsConfigurationImplementation.getItemMaterial(), "customMaterial"));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("confirmMessage", 15, OptionConfirmMessage.class, OptionConfirmMessage::new, null));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hologramText", 17, OptionHologramText.class,
				OptionHologramText::new, Lang.HologramText.toString()));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("bypassLimit", 18, OptionBypassLimit.class, OptionBypassLimit::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("startableFromGUI", 19, OptionStartable.class, OptionStartable::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("failOnDeath", 20, OptionFailOnDeath.class, OptionFailOnDeath::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("cancellable", 21, OptionCancellable.class, OptionCancellable::new, true));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("cancelActions", 22, OptionCancelRewards.class,
				OptionCancelRewards::new, new ArrayList<>()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hologramLaunch", 25, OptionHologramLaunch.class,
				OptionHologramLaunch::new, QuestsConfigurationImplementation.getHoloLaunchItem()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hologramLaunchNo", 26, OptionHologramLaunchNo.class,
				OptionHologramLaunchNo::new, QuestsConfigurationImplementation.getHoloLaunchNoItem()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("scoreboard", 27, OptionScoreboardEnabled.class,
				OptionScoreboardEnabled::new, true));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("hideNoRequirements", 28,
				OptionHideNoRequirements.class, OptionHideNoRequirements::new, false));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("auto", 29, OptionAutoQuest.class, OptionAutoQuest::new, false));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("repeatable", 30, OptionRepeatable.class,
				OptionRepeatable::new, false, "multiple"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("timer", 31, OptionTimer.class, OptionTimer::new,
				QuestsConfigurationImplementation.getTimeBetween()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("visibility", 32, OptionVisibility.class,
				OptionVisibility::new, Arrays.asList(QuestVisibilityLocation.values()), "hid", "hide"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("endSound", 34, OptionEndSound.class,
				OptionEndSound::new, QuestsConfigurationImplementation.getFinishSound()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("firework", 35, OptionFirework.class,
				OptionFirework::new, QuestsConfigurationImplementation.getDefaultFirework()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("requirements", 36, OptionRequirements.class,
				OptionRequirements::new, new ArrayList<>()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("startRewards", 38, OptionStartRewards.class,
				OptionStartRewards::new, new ArrayList<>(), "startRewardsList"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("startMessage", 39, OptionStartMessage.class,
				OptionStartMessage::new, QuestsConfigurationImplementation.getPrefix() + Lang.STARTED_QUEST.toString()));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("starterNPC", 40, OptionStarterNPC.class,
				OptionStarterNPC::new, null, "starterID"));
		QuestsAPI.getAPI().registerQuestOption(
				new QuestOptionCreator<>("startDialog", 41, OptionStartDialog.class, OptionStartDialog::new, null));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("endRewards", 43, OptionEndRewards.class,
				OptionEndRewards::new, new ArrayList<>(), "rewardsList"));
		QuestsAPI.getAPI().registerQuestOption(new QuestOptionCreator<>("endMsg", 44, OptionEndMessage.class,
				OptionEndMessage::new, QuestsConfigurationImplementation.getPrefix() + Lang.FINISHED_BASE.toString()));
	}

	public static void registerRewards() {
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("commandReward", CommandReward.class,
				ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.command.toString()), CommandReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("itemReward", ItemReward.class,
				ItemUtils.item(XMaterial.STONE_SWORD, Lang.rewardItems.toString()), ItemReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("removeItemsReward", RemoveItemsReward.class,
				ItemUtils.item(XMaterial.CHEST, Lang.rewardRemoveItems.toString()), RemoveItemsReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("textReward", MessageReward.class,
				ItemUtils.item(XMaterial.WRITABLE_BOOK, Lang.endMessage.toString()), MessageReward::new));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("tpReward", TeleportationReward.class,
				ItemUtils.item(XMaterial.ENDER_PEARL, Lang.location.toString()), TeleportationReward::new, false));
		QuestsAPI.getAPI().getRewards().register(new RewardCreator("expReward", XPReward.class,
				ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.rewardXP.toString()), XPReward::new));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("checkpointReward", CheckpointReward.class,
						ItemUtils.item(XMaterial.NETHER_STAR, Lang.rewardCheckpoint.toString()), CheckpointReward::new,
						false, QuestObjectLocation.STAGE));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("questStopReward", QuestStopReward.class,
						ItemUtils.item(XMaterial.BARRIER, Lang.rewardStopQuest.toString()), QuestStopReward::new, false,
						QuestObjectLocation.STAGE));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("requirementDependentReward", RequirementDependentReward.class,
						ItemUtils.item(XMaterial.REDSTONE, Lang.rewardWithRequirements.toString()),
						RequirementDependentReward::new, true).setCanBeAsync(true));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("randomReward", RandomReward.class,
						ItemUtils.item(XMaterial.EMERALD, Lang.rewardRandom.toString()), RandomReward::new, true)
								.setCanBeAsync(true));
		QuestsAPI.getAPI().getRewards()
				.register(new RewardCreator("wait", WaitReward.class,
						ItemUtils.item(XMaterial.CLOCK, Lang.rewardWait.toString()), WaitReward::new, true)
								.setCanBeAsync(true));
		if (MinecraftVersion.MAJOR >= 9)
			QuestsAPI.getAPI().getRewards().register(new RewardCreator("titleReward", TitleReward.class,
					ItemUtils.item(XMaterial.NAME_TAG, Lang.rewardTitle.toString()), TitleReward::new, false));
	}

	public static void registerRequirements() {
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("logicalOr", LogicalOrRequirement.class,
				ItemUtils.item(XMaterial.REDSTONE_TORCH, Lang.RLOR.toString()), LogicalOrRequirement::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("questRequired", QuestRequirement.class,
				ItemUtils.item(XMaterial.ARMOR_STAND, Lang.RQuest.toString()), QuestRequirement::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("levelRequired", LevelRequirement.class,
				ItemUtils.item(XMaterial.EXPERIENCE_BOTTLE, Lang.RLevel.toString()), LevelRequirement::new));
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("permissionRequired", PermissionsRequirement.class,
						ItemUtils.item(XMaterial.PAPER, Lang.RPermissions.toString()), PermissionsRequirement::new));
		QuestsAPI.getAPI().getRequirements()
				.register(new RequirementCreator("scoreboardRequired", ScoreboardRequirement.class,
						ItemUtils.item(XMaterial.COMMAND_BLOCK, Lang.RScoreboard.toString()), ScoreboardRequirement::new));
		if (MinecraftVersion.MAJOR >= 9)
			QuestsAPI.getAPI().getRequirements()
					.register(new RequirementCreator("equipmentRequired", EquipmentRequirement.class,
							ItemUtils.item(XMaterial.CHAINMAIL_HELMET, Lang.REquipment.toString()),
							EquipmentRequirement::new));
	}

	public static void registerItemComparisons() {
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("bukkit", Lang.comparisonBukkit.toString(),
				Lang.comparisonBukkitLore.toString(), ItemStack::isSimilar).setEnabledByDefault());
		QuestsAPI.getAPI().registerItemComparison(new ItemComparison("customBukkit", Lang.comparisonCustomBukkit.toString(),
				Lang.comparisonCustomBukkitLore.toString(), Utils::isSimilar));
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

}
