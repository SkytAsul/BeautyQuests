package fr.skytasul.quests.api.localization;

import fr.skytasul.quests.api.utils.messaging.MessageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * Stores all string paths and methods to format and send them to players.
 */
@SuppressWarnings("squid:S115")
public enum Lang implements Locale {

	/* Formats (must be first to be used after) */
	Prefix("misc.format.prefix"),
	NpcText("misc.format.npcText"), // 0: npc, 1: msg, 2: index, 3: max
	SelfText("misc.format.selfText"), // 0: player, 1: msg, 2: index, 3: max
	OffText("misc.format.offText"), // 1: msg
	EditorPrefix("misc.format.editorPrefix"),
	ErrorPrefix("misc.format.errorPrefix"),
	SuccessPrefix("misc.format.successPrefix"),
	RequirementNotMetPrefix("misc.format.requirementNotMetPrefix"),

	/* Messages */
	FINISHED_BASE("msg.quest.finished.base"),
	FINISHED_OBTAIN("msg.quest.finished.obtain"),
	STARTED_QUEST("msg.quest.started"),
	SUCCESFULLY_CREATED("msg.quest.created"),
	SUCCESFULLY_EDITED("msg.quest.edited"),
	CANCELLED("msg.quest.createCancelled"),
	QUEST_CANCEL("msg.quest.cancelling"),
	QUEST_EDIT_CANCEL("msg.quest.editCancelling"),
	QUEST_INVALID("msg.quest.invalidID"), // 0: quest id
	POOL_INVALID("msg.quest.invalidPoolID"), // 0: pool id
	ALREADY_STARTED("msg.quest.alreadyStarted"),
	QUEST_NOT_STARTED("msg.quest.notStarted"),

	QUESTS_MAX_LAUNCHED("msg.quests.maxLaunched"), // 0: max quests
	QUEST_UPDATED("msg.quests.updated"),
	QUEST_CHECKPOINT("msg.quests.checkpoint"),
	QUEST_FAILED("msg.quests.failed"),

	DIALOG_SKIPPED("msg.dialogs.skipped", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_TOO_FAR("msg.dialogs.tooFar"), // 0: npc name

	POOL_NO_TIME("msg.pools.noTime"), // 0: time left
	POOL_ALL_COMPLETED("msg.pools.allCompleted"),
	POOL_NO_AVAILABLE("msg.pools.noAvailable"),
	POOL_MAX_QUESTS("msg.pools.maxQuests"), // 0: max quests

	QUEST_ITEM_DROP("msg.questItem.drop"),
	QUEST_ITEM_CRAFT("msg.questItem.craft"),
	QUEST_ITEM_EAT("msg.questItem.eat"),
	QUEST_ITEM_PLACE("msg.questItem.place"),

	STAGE_MOBSLIST("msg.stageMobs.listMobs"),

	TYPE_CANCEL("msg.typeCancel"),
	NPC_TEXT("msg.writeNPCText"),
	REGION_NAME("msg.writeRegionName"),
	XP_GAIN("msg.writeXPGain"),
	MOB_AMOUNT("msg.writeMobAmount"),
	MOB_NAME("msg.writeMobName"),
	CHAT_MESSAGE("msg.writeChatMessage"),
	WRITE_MESSAGE("msg.writeMessage"),
	WRITE_START_MESSAGE("msg.writeStartMessage"),
	WRITE_END_MESSAGE("msg.writeEndMsg"),
	WRITE_END_SOUND("msg.writeEndSound"),
	DESC_MESSAGE("msg.writeDescriptionText"),
	START_TEXT("msg.writeStageText"),
	MOVE_TELEPORT_POINT("msg.moveToTeleportPoint"),
	NPC_NAME("msg.writeNpcName"),
	NPC_SKIN("msg.writeNpcSkinName"),
	QUEST_NAME("msg.writeQuestName"),
	COMMAND("msg.writeCommand"),
	COMMAND_DELAY("msg.writeCommandDelay"),
	HOLOGRAM_TEXT("msg.writeHologramText"),
	TIMER("msg.writeQuestTimer"),
	CONFIRM_MESSAGE("msg.writeConfirmMessage"),
	QUEST_DESCRIPTION("msg.writeQuestDescription"),
	QUEST_MATERIAL("msg.writeQuestMaterial"),

	REQUIREMENT_QUEST("msg.requirements.quest"),
	REQUIREMENT_LEVEL("msg.requirements.level"),
	REQUIREMENT_JOB("msg.requirements.job"),
	REQUIREMENT_SKILL("msg.requirements.skill"),
	REQUIREMENT_COMBAT_LEVEL("msg.requirements.combatLevel"),
	REQUIREMENT_MONEY("msg.requirements.money"), // 0: money
	QUEST_WAIT("msg.requirements.waitTime"), //0: time

	XP_EDITED("msg.experience.edited"),
	SELECT_KILL_NPC("msg.selectNPCToKill"),

	REGION_DOESNT_EXIST("msg.regionDoesntExists"),
	NPC_DOESNT_EXIST("msg.npcDoesntExist"),
	NUMBER_NEGATIVE("msg.number.negative"),
	NUMBER_ZERO("msg.number.zero"),
	NUMBER_INVALID("msg.number.invalid"),
	NUMBER_NOT_IN_BOUNDS("msg.number.notInBounds"), // 0: min, 1: max
	ERROR_OCCURED("msg.errorOccurred"),
	OUT_OF_BOUNDS("msg.indexOutOfBounds"),
	INVALID_BLOCK_DATA("msg.invalidBlockData"), // 0: blockdata, 1: material
	INVALID_BLOCK_TAG("msg.invalidBlockTag"), // 0: tag

	NEED_OBJECTS("msg.bringBackObjects"),
	ITEM_DROPPED("msg.inventoryFull"),

	VERSION_REQUIRED("msg.versionRequired", ErrorPrefix), // 0: version

	RESTART_SERVER("msg.restartServer"),

	// * Commands *

	COMMAND_DOESNT_EXIST_NOSLASH("msg.command.invalidCommand.simple"),
	ITEM_CHANGED("msg.command.itemChanged"),
	ITEM_REMOVED("msg.command.itemRemoved"),
	SUCCESFULLY_REMOVED("msg.command.removed"),
	LEAVE_ALL_RESULT("msg.command.leaveAll"),
	DATA_REMOVED("msg.command.resetPlayer.player"), // 0: quest amount, 1: player name, 2: pools amount
	DATA_REMOVED_INFO("msg.command.resetPlayer.remover"), // 0: quest amount, 1: player name, 2: pools amount
	DATA_QUEST_REMOVED("msg.command.resetPlayerQuest.player"),
	DATA_QUEST_REMOVED_INFO("msg.command.resetPlayerQuest.remover"),
	QUEST_PLAYERS_REMOVED("msg.command.resetQuest"), // 0: amount of players
	START_QUEST("msg.command.startQuest"),
	START_QUEST_NO_REQUIREMENT("msg.command.startQuestNoRequirements"),
	CANCEL_QUEST("msg.command.cancelQuest"),
	CANCEL_QUEST_UNAVAILABLE("msg.command.cancelQuestUnavailable"),
	BACKUP_CREATED("msg.command.backupCreated"),
	BACKUP_PLAYERS_FAILED("msg.command.backupPlayersFailed"),
	BACKUP_QUESTS_FAILED("msg.command.backupQuestsFailed"),
	ADMIN_MODE_ENTERED("msg.command.adminModeEntered"),
	ADMIN_MODE_LEFT("msg.command.adminModeLeft"),
	POOL_RESET_TIMER("msg.command.resetPlayerPool.timer"), // 0: pool ID, 1: player
	POOL_RESET_FULL("msg.command.resetPlayerPool.full"), // 0: pool ID, 1: player
	POOL_START_ERROR("msg.command.startPlayerPool.error", ErrorPrefix), // 0: pool ID, 1: player
	POOL_START_SUCCESS("msg.command.startPlayerPool.success", SuccessPrefix), // 0: pool ID, 1: player, 2: result
	POOL_COMPLETELY_RESET("msg.command.resetPool", SuccessPrefix), // 0: amount of players

	COMMAND_SCOREBOARD_LINESET("msg.command.scoreboard.lineSet"), // 0: line id
	COMMAND_SCOREBOARD_LINERESET("msg.command.scoreboard.lineReset"), // 0: line id
	COMMAND_SCOREBOARD_LINEREMOVE("msg.command.scoreboard.lineRemoved"), // 0: line id
	COMMAND_SCOREBOARD_LINENOEXIST("msg.command.scoreboard.lineInexistant"), // 0: line id
	COMMAND_SCOREBOARD_RESETALL("msg.command.scoreboard.resetAll"), // 0: player
	COMMAND_SCOREBOARD_HIDDEN("msg.command.scoreboard.hidden"), // 0: player
	COMMAND_SCOREBOARD_SHOWN("msg.command.scoreboard.shown"), // 0: player
	COMMAND_SCOREBOARD_OWN_HIDDEN("msg.command.scoreboard.own.hidden"),
	COMMAND_SCOREBOARD_OWN_SHOWN("msg.command.scoreboard.own.shown"),
	COMMAND_SETSTAGE_BRANCH_DOESNTEXIST("msg.command.setStage.branchDoesntExist"),
	COMMAND_SETSTAGE_STAGE_DOESNTEXIST("msg.command.setStage.doesntExist"),
	COMMAND_SETSTAGE_NEXT("msg.command.setStage.next"),
	COMMAND_SETSTAGE_NEXT_UNAVAILABLE("msg.command.setStage.nextUnavailable"),
	COMMAND_SETSTAGE_SET("msg.command.setStage.set"),
	COMMAND_STARTDIALOG_IMPOSSIBLE("msg.command.startDialog.impossible"),
	COMMAND_STARTDIALOG_NO("msg.command.startDialog.noDialog"),
	COMMAND_STARTDIALOG_ALREADY("msg.command.startDialog.alreadyIn"),
	COMMAND_STARTDIALOG_SUCCESS("msg.command.startDialog.success"), // 0: player, 1: quest id
	COMMAND_CHECKPOINT_NO("msg.command.checkpoint.noCheckpoint"), // 0: quest name
	COMMAND_CHECKPOINT_NOT_STARTED("msg.command.checkpoint.questNotStarted"),
	COMMAND_TRANSLATION_SYNTAX("msg.command.downloadTranslations.syntax"),
	COMMAND_TRANSLATION_NOT_FOUND("msg.command.downloadTranslations.notFound"), // 0: lang, 1: version
	COMMAND_TRANSLATION_EXISTS("msg.command.downloadTranslations.exists"), // 0: file
	COMMAND_TRANSLATION_DOWNLOADED("msg.command.downloadTranslations.downloaded"), // 0: lang

	COMMAND_HELP("msg.command.help.header", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_CREATE("msg.command.help.create", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_EDIT("msg.command.help.edit", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_REMOVE("msg.command.help.remove", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_FINISH("msg.command.help.finishAll", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_STAGE("msg.command.help.setStage", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_DIALOG("msg.command.help.startDialog", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_RESET("msg.command.help.resetPlayer", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_RESETQUEST("msg.command.help.resetPlayerQuest", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_SEE("msg.command.help.seePlayer", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_RELOAD("msg.command.help.reload", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_START("msg.command.help.start", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_SETITEM("msg.command.help.setItem", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_SETFIREWORK("msg.command.help.setFirework", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_ADMINMODE("msg.command.help.adminMode", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_VERSION("msg.command.help.version", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_DOWNLOAD_TRANSLATIONS("msg.command.help.downloadTranslations", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_SAVE("msg.command.help.save", MessageType.DefaultMessageType.UNPREFIXED),
	COMMAND_HELP_LIST("msg.command.help.list", MessageType.DefaultMessageType.UNPREFIXED),

	// * Editors *
	ALREADY_EDITOR("msg.editor.already"),
	ENTER_EDITOR_TITLE("msg.editor.enter.title"),
	ENTER_EDITOR_SUB("msg.editor.enter.subtitle"),
	ENTER_EDITOR_LIST("msg.editor.enter.list"),
	CHAT_EDITOR("msg.editor.chat"),
	NPC_EDITOR_ENTER("msg.editor.npc.enter"),

	ARG_NOT_SUPPORTED("msg.editor.text.argNotSupported"),

	CHOOSE_NPC_STARTER("msg.editor.npc.choseStarter"),
	NPC_NOT_QUEST("msg.editor.npc.notStarter"),

	CLICK_BLOCK("msg.editor.selectWantedBlock"),
	BLOCKS_AMOUNT("msg.editor.blockAmount"),
	BLOCK_NAME("msg.editor.blockName"),
	BLOCK_DATA("msg.editor.blockData"), // 0: available block datas
	BLOCK_TAGS("msg.editor.blockTag"), // 0: available block tags

	BUCKET_AMOUNT("msg.editor.typeBucketAmount"),
	DAMAGE_AMOUNT("msg.editor.typeDamageAmount", EditorPrefix),

	LOCATION_GO("msg.editor.goToLocation"),
	LOCATION_RADIUS("msg.editor.typeLocationRadius"),
	LOCATION_WORLDPATTERN("msg.editor.stage.location.typeWorldPattern"),

	GAME_TICKS("msg.editor.typeGameTicks"),

	AVAILABLE_ELEMENTS("msg.editor.availableElements"), // 0: available elements
	NO_SUCH_ELEMENT("msg.editor.noSuchElement", EditorPrefix), // 0: available elements
	INVALID_PATTERN("msg.editor.invalidPattern"), // 0: pattern

	COMPARISON_TYPE("msg.editor.comparisonTypeDefault"), // 0: available comparisons, 1: default comparison

	SCOREBOARD_OBJECTIVE_NOT_FOUND("msg.editor.scoreboardObjectiveNotFound"),

	POOL_HOLOGRAM_TEXT("msg.editor.pool.hologramText", EditorPrefix),
	POOL_MAXQUESTS("msg.editor.pool.maxQuests", EditorPrefix),
	POOL_QUESTS_PER_LAUNCH("msg.editor.pool.questsPerLaunch", EditorPrefix),
	POOL_TIME("msg.editor.pool.timeMsg", EditorPrefix),

	TITLE_TITLE("msg.editor.title.title", EditorPrefix),
	TITLE_SUBTITLE("msg.editor.title.subtitle", EditorPrefix),
	TITLE_FADEIN("msg.editor.title.fadeIn", EditorPrefix),
	TITLE_STAY("msg.editor.title.stay", EditorPrefix),
	TITLE_FADEOUT("msg.editor.title.fadeOut", EditorPrefix),

	COLOR_NAMED_EDITOR("msg.editor.colorNamed", EditorPrefix),
	COLOR_EDITOR("msg.editor.color", EditorPrefix),
	INVALID_COLOR("msg.editor.invalidColor", ErrorPrefix),

	FIREWORK_INVALID("msg.editor.firework.invalid", ErrorPrefix),
	FIREWORK_INVALID_HAND("msg.editor.firework.invalidHand", ErrorPrefix),
	FIREWORK_EDITED("msg.editor.firework.edited", SuccessPrefix),
	FIREWORK_REMOVED("msg.editor.firework.removed", SuccessPrefix),

	// requirements
	CHOOSE_XP_REQUIRED("msg.editor.text.chooseLvlRequired"),
	CHOOSE_JOB_REQUIRED("msg.editor.text.chooseJobRequired"),
	CHOOSE_PERM_REQUIRED("msg.editor.text.choosePermissionRequired"),
	CHOOSE_PERM_REQUIRED_MESSAGE("msg.editor.text.choosePermissionMessage"),
	CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER("msg.editor.text.choosePlaceholderRequired.identifier"),
	CHOOSE_PLACEHOLDER_REQUIRED_VALUE("msg.editor.text.choosePlaceholderRequired.value"),
	CHOOSE_SKILL_REQUIRED("msg.editor.text.chooseSkillRequired"),
	CHOOSE_MONEY_REQUIRED("msg.editor.text.chooseMoneyRequired"),
	CHOOSE_SCOREBOARD_OBJECTIVE("msg.editor.text.chooseObjectiveRequired"),
	CHOOSE_SCOREBOARD_TARGET("msg.editor.text.chooseObjectiveTargetScore"),
	CHOOSE_REGION_REQUIRED("msg.editor.text.chooseRegionRequired"),
	CHOOSE_REQUIREMENT_CUSTOM_REASON("msg.editor.text.chooseRequirementCustomReason", EditorPrefix),
	CHOOSE_REQUIREMENT_CUSTOM_DESCRIPTION("msg.editor.text.chooseRequirementCustomDescription", EditorPrefix),
	CHOOSE_REWARD_CUSTOM_DESCRIPTION("msg.editor.text.chooseRewardCustomDescription", EditorPrefix),

	// rewards
	CHOOSE_PERM_REWARD("msg.editor.text.reward.permissionName"),
	CHOOSE_PERM_WORLD("msg.editor.text.reward.permissionWorld"),
	CHOOSE_MONEY_REWARD("msg.editor.text.reward.money"),
	REWARD_EDITOR_WAIT("msg.editor.text.reward.wait"),
	REWARD_EDITOR_RANDOM_MIN("msg.editor.text.reward.random.min"),
	REWARD_EDITOR_RANDOM_MAX("msg.editor.text.reward.random.max"),

	CHOOSE_ITEM_TYPE("msg.editor.itemCreator.itemType"),
	CHOOSE_ITEM_AMOUNT("msg.editor.itemCreator.itemAmount"),
	CHOOSE_ITEM_NAME("msg.editor.itemCreator.itemName"),
	CHOOSE_ITEM_LORE("msg.editor.itemCreator.itemLore"),
	UNKNOWN_ITEM_TYPE("msg.editor.itemCreator.unknownItemType"),
	INVALID_ITEM_TYPE("msg.editor.itemCreator.invalidItemType"),
	UNKNOWN_BLOCK_TYPE("msg.editor.itemCreator.unknownBlockType"),
	INVALID_BLOCK_TYPE("msg.editor.itemCreator.invalidBlockType"),

	DIALOG_MESSAGE_SYNTAX("msg.editor.dialog.syntaxMessage"),
	DIALOG_REMOVE_SYNTAX("msg.editor.dialog.syntaxRemove"),
	DIALOG_MSG_ADDED_PLAYER("msg.editor.dialog.player"),
	DIALOG_MSG_ADDED_NPC("msg.editor.dialog.npc"),
	DIALOG_MSG_ADDED_NOSENDER("msg.editor.dialog.noSender"),
	DIALOG_MSG_EDITED("msg.editor.dialog.edited"),
	DIALOG_MSG_REMOVED("msg.editor.dialog.messageRemoved"),
	DIALOG_SOUND_ADDED("msg.editor.dialog.soundAdded"),
	DIALOG_TIME_SET("msg.editor.dialog.timeSet"), // 0: index, 1: time
	DIALOG_TIME_REMOVED("msg.editor.dialog.timeRemoved"), // 0: index
	DIALOG_NPCNAME_SET("msg.editor.dialog.npcName.set"), // 0: previous, 1: new
	DIALOG_NPCNAME_UNSET("msg.editor.dialog.npcName.unset"), // 0: previous
	DIALOG_SKIPPABLE_SET("msg.editor.dialog.skippable.set"), // 0: previous, 1: new
	DIALOG_SKIPPABLE_UNSET("msg.editor.dialog.skippable.unset"), // 0: previous
	DIALOG_CLEARED("msg.editor.dialog.cleared"),
	DIALOG_HELP_HEADER("msg.editor.dialog.help.header", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_NPC("msg.editor.dialog.help.npc", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_PLAYER("msg.editor.dialog.help.player", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_NOTHING("msg.editor.dialog.help.nothing", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_REMOVE("msg.editor.dialog.help.remove", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_LIST("msg.editor.dialog.help.list", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_NPCINSERT("msg.editor.dialog.help.npcInsert", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_PLAYERINSERT("msg.editor.dialog.help.playerInsert", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_NOTHINGINSERT("msg.editor.dialog.help.nothingInsert", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_EDIT("msg.editor.dialog.help.edit", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_ADDSOUND("msg.editor.dialog.help.addSound", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_SETTIME("msg.editor.dialog.help.setTime", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_NPCNAME("msg.editor.dialog.help.npcName", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_SKIPPABLE("msg.editor.dialog.help.skippable", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_CLEAR("msg.editor.dialog.help.clear", MessageType.DefaultMessageType.UNPREFIXED),
	DIALOG_HELP_CLOSE("msg.editor.dialog.help.close", MessageType.DefaultMessageType.UNPREFIXED),

	MYTHICMOB_LIST("msg.editor.mythicmobs.list"),
	MYTHICMOB_NOT_EXISTS("msg.editor.mythicmobs.isntMythicMob"),
	MYTHICMOB_DISABLED("msg.editor.mythicmobs.disabled"),
	ADVANCED_SPAWNERS_MOB("msg.editor.advancedSpawnersMob", EditorPrefix),

	TEXTLIST_SYNTAX("msg.editor.textList.syntax"),
	TEXTLIST_TEXT_ADDED("msg.editor.textList.added"),
	TEXTLIST_TEXT_REMOVED("msg.editor.textList.removed"),
	TEXTLIST_TEXT_HELP_HEADER("msg.editor.textList.help.header", MessageType.DefaultMessageType.UNPREFIXED),
	TEXTLIST_TEXT_HELP_ADD("msg.editor.textList.help.add", MessageType.DefaultMessageType.UNPREFIXED),
	TEXTLIST_TEXT_HELP_REMOVE("msg.editor.textList.help.remove", MessageType.DefaultMessageType.UNPREFIXED),
	TEXTLIST_TEXT_HELP_LIST("msg.editor.textList.help.list", MessageType.DefaultMessageType.UNPREFIXED),
	TEXTLIST_TEXT_HELP_CLOSE("msg.editor.textList.help.close", MessageType.DefaultMessageType.UNPREFIXED),

	// * Quests lists*

	Finished("advancement.finished"),
	Not_Started("advancement.notStarted"),

	/* Inventories */
	done("inv.validate"),
	cancel("inv.cancel"),
	search("inv.search"),
	addObject("inv.addObject"),

	INVENTORY_CONFIRM("inv.confirm.name"),
	confirmYes("inv.confirm.yes"),
	confirmNo("inv.confirm.no"),

	stageCreate("inv.create.stageCreate"),
	stageRemove("inv.create.stageRemove"),
	stageUp("inv.create.stageUp"),
	stageDown("inv.create.stageDown"),
	stageType("inv.create.stageType"),
	cantFinish("inv.create.cantFinish"),
	stageNPC("inv.create.findNPC"),
	stageBring("inv.create.bringBack"),
	stageGoTo("inv.create.findRegion"),
	stageMobs("inv.create.killMobs"),
	stageMine("inv.create.mineBlocks"),
	stagePlace("inv.create.placeBlocks"),
	stageChat("inv.create.talkChat"),
	stageInteractBlock("inv.create.interact"),
	stageInteractLocation("inv.create.interactLocation"),
	stageFish("inv.create.fish"),
	stageMelt("inv.create.melt"),
	stageEnchant("inv.create.enchant"),
	stageCraft("inv.create.craft"),
	stageBucket("inv.create.bucket"),
	stageLocation("inv.create.location"),
	stagePlayTime("inv.create.playTime"),
	stageBreedAnimals("inv.create.breedAnimals"),
	stageTameAnimals("inv.create.tameAnimals"),
	stageDeath("inv.create.death"),
	stageDealDamage("inv.create.dealDamage"),
	stageEatDrink("inv.create.eatDrink"),
	stageText("inv.create.NPCText"),
	stageNPCSelect("inv.create.NPCSelect"),
	stageHide("inv.create.hideClues"),
	editMobs("inv.create.editMobsKill"),
	mobsKillType("inv.create.mobsKillFromAFar"),
	editBlocksMine("inv.create.editBlocksMine"),
	preventBlockPlace("inv.create.preventBlockPlace"),
	editBlocksPlace("inv.create.editBlocksPlace"),
	editMessage("inv.create.editMessageType"),
	cancelEvent("inv.create.cancelMessage"),
	ignoreCase("inv.create.ignoreCase"),
	placeholders("inv.create.replacePlaceholders"),
	stageItems("inv.create.selectItems"),
	stageItemsMessage("inv.create.selectItemsMessage"),
	stageItemsComparison("inv.create.selectItemsComparisons"),
	stageRegion("inv.create.selectRegion"),
	stageRegionExit("inv.create.toggleRegionExit"),
	startMsg("inv.create.stageStartMsg"),
	blockLocation("inv.create.selectBlockLocation"),
	blockMaterial("inv.create.selectBlockMaterial"),
	leftClick("inv.create.leftClick"),
	editFishes("inv.create.editFishes"),
	editItemsToMelt("inv.create.editItemsToMelt"),
	editItemsToEnchant("inv.create.editItemsToEnchant"),
	editItem("inv.create.editItem"),
	editBucketType("inv.create.editBucketType"),
	editBucketAmount("inv.create.editBucketAmount"),
	changeTicksRequired("inv.create.changeTicksRequired"),
	changeEntityType("inv.create.changeEntityType"),

	stageLocationLocation("inv.create.editLocation"),
	stageLocationRadius("inv.create.editRadius"),
	stageLocationCurrentRadius("inv.create.currentRadius"), // 0: radius
	stageLocationWorldPattern("inv.create.stage.location.worldPattern"),
	stageLocationWorldPatternLore("inv.create.stage.location.worldPatternLore"),

	stageDeathCauses("inv.create.stage.death.causes"),
	stageDeathCauseAny("inv.create.stage.death.anyCause"),
	stageDeathCausesSet("inv.create.stage.death.setCauses"), // 0: causes amount

	stageDealDamageValue("inv.create.stage.dealDamage.damage"),
	stageDealDamageMobs("inv.create.stage.dealDamage.targetMobs"),

	stageEatDrinkItems("inv.create.stage.eatDrink.items"),

	stagePlayTimeChangeTimeMode("inv.create.stage.playTime.changeTimeMode"),
	stagePlayTimeModeOnline("inv.create.stage.playTime.modes.online"),
	stagePlayTimeModeOffline("inv.create.stage.playTime.modes.offline"),
	stagePlayTimeModeRealtime("inv.create.stage.playTime.modes.realtime"),

	INVENTORY_STAGES("inv.stages.name"),
	nextPage("inv.stages.nextPage"),
	laterPage("inv.stages.laterPage"),
	regularPage("inv.stages.regularPage"),
	branchesPage("inv.stages.branchesPage"),
	ending("inv.stages.endingItem"),
	validationRequirements("inv.stages.validationRequirements"),
	validationRequirementsLore("inv.stages.validationRequirementsLore"),
	descMessage("inv.stages.descriptionTextItem"),
	previousBranch("inv.stages.previousBranch"),
	newBranch("inv.stages.newBranch"),

	INVENTORY_DETAILS("inv.details.name"),
	multiple("inv.details.multipleTime.itemName"),
	multipleLore("inv.details.multipleTime.itemLore"),
	cancellable("inv.details.cancellable"),
	cancellableLore("inv.details.cancellableLore"),
	startableFromGUI("inv.details.startableFromGUI"),
	startableFromGUILore("inv.details.startableFromGUILore"),
	scoreboard("inv.details.scoreboardItem"),
	scoreboardLore("inv.details.scoreboardItemLore"),
	hideNoRequirements("inv.details.hideNoRequirementsItem"),
	hideNoRequirementsLore("inv.details.hideNoRequirementsItemLore"),
	bypass("inv.details.bypassLimit"),
	bypassLore("inv.details.bypassLimitLore"),
	auto("inv.details.auto"),
	autoLore("inv.details.autoLore"),
	questName("inv.details.questName"),
	questNameLore("inv.details.questNameLore"),
	rewardItems("inv.details.setItemsRewards"),
	rewardRemoveItems("inv.details.removeItemsReward"),
	rewardXP("inv.details.setXPRewards"),
	rewardCheckpoint("inv.details.setCheckpointReward"),
	rewardStopQuest("inv.details.setRewardStopQuest"),
	rewardWithRequirements("inv.details.setRewardsWithRequirements"),
	rewardRandom("inv.details.setRewardsRandom"),
	rewardPerm("inv.details.setPermReward"),
	rewardMoney("inv.details.setMoneyReward"),
	rewardWait("inv.details.setWaitReward"),
	rewardTitle("inv.details.setTitleReward"),
	questStarterSelect("inv.details.selectStarterNPC"),
	questStarterSelectLore("inv.details.selectStarterNPCLore"),
	questStarterSelectPool("inv.details.selectStarterNPCPool"),
	create("inv.details.createQuestName"),
	createLore("inv.details.createQuestLore"),
	edit("inv.details.editQuestName"),
	endMessage("inv.details.endMessage"),
	endMessageLore("inv.details.endMessageLore"),
	endSound("inv.details.endSound"),
	endSoundLore("inv.details.endSoundLore"),
	startMessage("inv.details.startMessage"),
	startMessageLore("inv.details.startMessageLore"),
	startDialog("inv.details.startDialog"),
	startDialogLore("inv.details.startDialogLore"),
	editRequirements("inv.details.editRequirements"),
	editRequirementsLore("inv.details.editRequirementsLore"),
	startRewards("inv.details.startRewards"),
	startRewardsLore("inv.details.startRewardsLore"),
	cancelRewards("inv.details.cancelRewards"),
	cancelRewardsLore("inv.details.cancelRewardsLore"),
	hologramText("inv.details.hologramText"),
	hologramTextLore("inv.details.hologramTextLore"),
	timer("inv.details.timer"),
	timerLore("inv.details.timerLore"),
	requirements("inv.details.requirements"),
	rewards("inv.details.rewards"),
	actions("inv.details.actions"),
	rewardsLore("inv.details.rewardsLore"),
	hologramLaunch("inv.details.hologramLaunch"),
	hologramLaunchLore("inv.details.hologramLaunchLore"),
	hologramLaunchNo("inv.details.hologramLaunchNo"),
	hologramLaunchNoLore("inv.details.hologramLaunchNoLore"),
	customConfirmMessage("inv.details.customConfirmMessage"),
	customConfirmMessageLore("inv.details.customConfirmMessageLore"),
	customDescription("inv.details.customDescription"),
	customDescriptionLore("inv.details.customDescriptionLore"),
	customMaterial("inv.details.customMaterial"),
	customMaterialLore("inv.details.customMaterialLore"),
	failOnDeath("inv.details.failOnDeath"),
	failOnDeathLore("inv.details.failOnDeathLore"),
	questPool("inv.details.questPool"),
	questPoolLore("inv.details.questPoolLore"),
	optionFirework("inv.details.firework"),
	optionFireworkLore("inv.details.fireworkLore"),
	optionFireworkDrop("inv.details.fireworkLoreDrop"),
	optionVisibility("inv.details.visibility"),
	optionVisibilityLore("inv.details.visibilityLore"),
	keepDatas("inv.details.keepDatas"),
	keepDatasLore("inv.details.keepDatasLore"),
	resetLore("inv.details.loreReset"),
	optionValue("inv.details.optionValue"), // 0: value
	defaultValue("inv.details.defaultValue"),
	requiredParameter("inv.details.requiredParameter"),

	INVENTORY_ITEMS("inv.itemsSelect.name"),
	itemsNone("inv.itemsSelect.none"),

	INVENTORY_ITEM("inv.itemSelect.name"),

	INVENTORY_NPC("inv.npcCreate.name"),
	name("inv.npcCreate.setName"),
	skin("inv.npcCreate.setSkin"),
	npcType("inv.npcCreate.setType"),
	move("inv.npcCreate.move.itemName"),
	moveLore("inv.npcCreate.move.itemLore"),
	moveItem("inv.npcCreate.moveItem"),

	INVENTORY_SELECT("inv.npcSelect.name"),
	selectNPC("inv.npcSelect.selectStageNPC"),
	createNPC("inv.npcSelect.createStageNPC"),

	INVENTORY_TYPE("inv.entityType.name"),
	INVENTORY_CHOOSE("inv.chooseQuest.name"),
	questMenu("inv.chooseQuest.menu"),
	questMenuLore("inv.chooseQuest.menuLore"),

	INVENTORY_MOBS("inv.mobs.name"),
	editAmount("inv.mobs.editAmount"),
	editMobName("inv.mobs.editMobName"),
	setLevel("inv.mobs.setLevel"),

	INVENTORY_MOBSELECT("inv.mobSelect.name"),
	bukkitMob("inv.mobSelect.bukkitEntityType"),
	mythicMob("inv.mobSelect.mythicMob"),
	epicBoss("inv.mobSelect.epicBoss"),
	boss("inv.mobSelect.boss"),
	advancedSpawners("inv.mobSelect.advancedSpawners"),

	location("inv.stageEnding.locationTeleport"),
	command("inv.stageEnding.command"),

	INVENTORY_REQUIREMENTS("inv.requirements.name"),
	setRequirementReason("inv.requirements.setReason"),
	requirementReason("inv.requirements.reason"), // 0: message

	INVENTORY_REWARDS("inv.rewards.name"),
	commands("inv.rewards.commands"),
	rewardRandomRewards("inv.rewards.random.rewards"),
	rewardRandomMinMax("inv.rewards.random.minMax"),

	INVENTORY_CHECKPOINT_ACTIONS("inv.checkpointActions.name"),

	INVENTORY_CANCEL_ACTIONS("inv.cancelActions.name"),

	INVENTORY_REWARDS_WITH_REQUIREMENTS("inv.rewardsWithRequirements.name"),

	INVENTORY_QUESTS_LIST("inv.listAllQuests.name"),
	INVENTORY_PLAYER_LIST("inv.listPlayerQuests.name"),
	notStarteds("inv.listQuests.notStarted"),
	finisheds("inv.listQuests.finished"),
	inProgress("inv.listQuests.inProgress"),
	dialogsHistoryLore("inv.listQuests.loreDialogsHistoryClick"),
	cancelLore("inv.listQuests.loreCancelClick"),
	startLore("inv.listQuests.loreStart"),
	startImpossibleLore("inv.listQuests.loreStartUnavailable"),
	timeWait("inv.listQuests.timeToWaitRedo"),
	canRedo("inv.listQuests.canRedo"),
	timesFinished("inv.listQuests.timesFinished"), // 0: times finished
	formatNormal("inv.listQuests.format.normal"),
	formatId("inv.listQuests.format.withId"),

	INVENTORY_CREATOR("inv.itemCreator.name"),
	itemType("inv.itemCreator.itemType"),
	itemFlags("inv.itemCreator.itemFlags"),
	itemName("inv.itemCreator.itemName"),
	itemLore("inv.itemCreator.itemLore"),
	itemQuest("inv.itemCreator.isQuestItem"),

	INVENTORY_COMMAND("inv.command.name"),
	commandValue("inv.command.value"),
	commandConsole("inv.command.console"),
	commandParse("inv.command.parse"),
	commandDelay("inv.command.delay"),

	INVENTORY_COMMANDS_LIST("inv.commandsList.name"),
	commandsListValue("inv.commandsList.value"),
	commandsListConsole("inv.commandsList.console"),

	INVENTORY_CHOOSEACCOUNT("inv.chooseAccount.name"),

	INVENTORY_BLOCK("inv.block.name"),
	materialName("inv.block.material"),
	materialNotItemLore("inv.block.materialNotItemLore"), // 0: block id
	blockName("inv.block.blockName"),
	blockData("inv.block.blockData"),
	blockTag("inv.block.blockTag"),
	blockTagLore("inv.block.blockTagLore"),

	INVENTORY_BLOCKSLIST("inv.blocksList.name"),

	INVENTORY_BLOCK_ACTION("inv.blockAction.name"),
	clickLocation("inv.blockAction.location"),
	clickMaterial("inv.blockAction.material"),

	INVENTORY_BUCKETS("inv.buckets.name"),

	INVENTORY_PERMISSION("inv.permission.name"),
	perm("inv.permission.perm"),
	world("inv.permission.world"),
	worldGlobal("inv.permission.worldGlobal"),
	permRemove("inv.permission.remove"),
	permRemoveLore("inv.permission.removeLore"),

	INVENTORY_PERMISSION_LIST("inv.permissionList.name"),
	permRemoved("inv.permissionList.removed"),
	permWorld("inv.permissionList.world"),

	INVENTORY_CLASSES_REQUIRED("inv.classesRequired.name"),
	INVENTORY_CLASSES_LIST("inv.classesList.name"),

	INVENTORY_FACTIONS_REQUIRED("inv.factionsRequired.name"),
	INVENTORY_FACTIONS_LIST("inv.factionsList.name"),

	INVENTORY_POOLS_MANAGE("inv.poolsManage.name"),
	poolItemName("inv.poolsManage.itemName"), // 0: pool ID
	poolItemNPC("inv.poolsManage.poolNPC"),
	poolItemMaxQuests("inv.poolsManage.poolMaxQuests"),
	poolItemQuestsPerLaunch("inv.poolsManage.poolQuestsPerLaunch"),
	poolItemRedo("inv.poolsManage.poolRedo"),
	poolItemTime("inv.poolsManage.poolTime"),
	poolItemHologram("inv.poolsManage.poolHologram"),
	poolItemAvoidDuplicates("inv.poolsManage.poolAvoidDuplicates"),
	poolItemQuestsList("inv.poolsManage.poolQuestsList"), // 0: size, 1: quests
	poolEdit("inv.poolsManage.edit"),
	poolChoose("inv.poolsManage.choose"),
	poolCreate("inv.poolsManage.create"),

	INVENTORY_POOL_CREATE("inv.poolCreation.name"),
	poolEditHologramText("inv.poolCreation.hologramText"),
	poolMaxQuests("inv.poolCreation.maxQuests"),
	poolQuestsPerLaunch("inv.poolCreation.questsPerLaunch"),
	poolTime("inv.poolCreation.time"),
	poolRedo("inv.poolCreation.redoAllowed"),
	poolAvoidDuplicates("inv.poolCreation.avoidDuplicates"),
	poolAvoidDuplicatesLore("inv.poolCreation.avoidDuplicatesLore"),
	poolRequirements("inv.poolCreation.requirements"),

	INVENTORY_POOLS_LIST("inv.poolsList.name"),

	INVENTORY_ITEM_COMPARISONS("inv.itemComparisons.name"),
	comparisonBukkit("inv.itemComparisons.bukkit"),
	comparisonBukkitLore("inv.itemComparisons.bukkitLore"),
	comparisonCustomBukkit("inv.itemComparisons.customBukkit"),
	comparisonCustomBukkitLore("inv.itemComparisons.customBukkitLore"),
	comparisonMaterial("inv.itemComparisons.material"),
	comparisonMaterialLore("inv.itemComparisons.materialLore"),
	comparisonName("inv.itemComparisons.itemName"),
	comparisonNameLore("inv.itemComparisons.itemNameLore"),
	comparisonLore("inv.itemComparisons.itemLore"),
	comparisonLoreLore("inv.itemComparisons.itemLoreLore"),
	comparisonEnchants("inv.itemComparisons.enchants"),
	comparisonEnchantsLore("inv.itemComparisons.enchantsLore"),
	comparisonRepairCost("inv.itemComparisons.repairCost"),
	comparisonRepairCostLore("inv.itemComparisons.repairCostLore"),
	comparisonItemsAdder("inv.itemComparisons.itemsAdder"),
	comparisonItemsAdderLore("inv.itemComparisons.itemsAdderLore"),
	comparisonMmoItems("inv.itemComparisons.mmoItems"),
	comparisonMmoItemsLore("inv.itemComparisons.mmoItemsLore"),

	INVENTORY_EDIT_TITLE("inv.editTitle.name"),
	title_title("inv.editTitle.title"),
	title_subtitle("inv.editTitle.subtitle"),
	title_fadeIn("inv.editTitle.fadeIn"),
	title_stay("inv.editTitle.stay"),
	title_fadeOut("inv.editTitle.fadeOut"),

	INVENTORY_PARTICLE_EFFECT("inv.particleEffect.name"),
	particle_shape("inv.particleEffect.shape"),
	particle_type("inv.particleEffect.type"),
	particle_color("inv.particleEffect.color"),

	INVENTORY_PARTICLE_LIST("inv.particleList.name"),
	particle_colored("inv.particleList.colored"),

	INVENTORY_DAMAGE_CAUSE("inv.damageCause.name"),

	INVENTORY_DAMAGE_CAUSES_LIST("inv.damageCausesList.name"),

	INVENTORY_VISIBILITY("inv.visibility.name"),
	visibility_notStarted("inv.visibility.notStarted"),
	visibility_inProgress("inv.visibility.inProgress"),
	visibility_finished("inv.visibility.finished"),
	visibility_maps("inv.visibility.maps"),

	INVENTORY_EQUIPMENT_SLOTS("inv.equipmentSlots.name"),

	object_description_set("inv.questObjects.setCustomDescription"),
	object_description("inv.questObjects.description"), // 0: description

	BOOK_NAME("inv.listBook.questName"),
	BOOK_STARTER("inv.listBook.questStarter"),
	BOOK_REWARDS("inv.listBook.questRewards"),
	BOOK_SEVERAL("inv.listBook.questMultiple"),
	BOOK_REQUIREMENTS("inv.listBook.requirements"),
	BOOK_STAGES("inv.listBook.questStages"),
	BOOK_NOQUEST("inv.listBook.noQuests"),

	/* Scoreboard */

	SCOREBOARD_NAME("scoreboard.name"),
	SCOREBOARD_NONE("scoreboard.noLaunched"),
	SCOREBOARD_NONE_NAME("scoreboard.noLaunchedName"),
	SCOREBOARD_NONE_DESC("scoreboard.noLaunchedDescription"),
	SCOREBOARD_BETWEEN_BRANCHES("scoreboard.textBetwteenBranch"),
	SCOREBOARD_ASYNC_END("scoreboard.asyncEnd"),
	SCOREBOARD_REG("scoreboard.stage.region"),
	SCOREBOARD_NPC("scoreboard.stage.npc"),
	SCOREBOARD_ITEMS("scoreboard.stage.items"),
	SCOREBOARD_MOBS("scoreboard.stage.mobs"),
	SCOREBOARD_MINE("scoreboard.stage.mine"),
	SCOREBOARD_PLACE("scoreboard.stage.placeBlocks"),
	SCOREBOARD_CHAT("scoreboard.stage.chat"),
	SCOREBOARD_INTERACT_LOCATION("scoreboard.stage.interact"),
	SCOREBOARD_INTERACT_MATERIAL("scoreboard.stage.interactMaterial"),
	SCOREBOARD_FISH("scoreboard.stage.fish"),
	SCOREBOARD_MELT("scoreboard.stage.melt"),
	SCOREBOARD_ENCHANT("scoreboard.stage.enchant"),
	SCOREBOARD_CRAFT("scoreboard.stage.craft"),
	SCOREBOARD_BUCKET("scoreboard.stage.bucket"),
	SCOREBOARD_BREED("scoreboard.stage.breed"), // 0: animals to breed
	SCOREBOARD_TAME("scoreboard.stage.tame"), // 0: animals to breed
	SCOREBOARD_LOCATION("scoreboard.stage.location"), // 0: x, 1: y, 2: z, 3: world
	SCOREBOARD_PLAY_TIME("scoreboard.stage.playTimeFormatted"), // 0: remaining time
	SCOREBOARD_DIE("scoreboard.stage.die"),
	SCOREBOARD_DEAL_DAMAGE_ANY("scoreboard.stage.dealDamage.any"), // 0: damage
	SCOREBOARD_DEAL_DAMAGE_MOBS("scoreboard.stage.dealDamage.mobs"), // 0: damage, 1: mobs
	SCOREBOARD_EAT_DRINK("scoreboard.stage.eatDrink"), // 0: items

	/* Indications */

	INDICATION_START("indication.startQuest"), // 0: quest name
	INDICATION_CLOSE("indication.closeInventory"),
	INDICATION_CANCEL("indication.cancelQuest"), // 0: quest name
	INDICATION_REMOVE("indication.removeQuest"), // 0: quest name
	INDICATION_REMOVE_POOL("indication.removePool"),

	/* Description */

	RDTitle("description.requirement.title"),
	RDLevel("description.requirement.level"), // 0: lvl
	RDJobLevel("description.requirement.jobLevel"), // 0: lvl, 1: job
	RDCombatLevel("description.requirement.combatLevel"), // 0: lvl
	RDSkillLevel("description.requirement.skillLevel"), // 0: lvl, 1: skill
	RDClass("description.requirement.class"), // 0: classes
	RDFaction("description.requirement.faction"), // 0: factions
	RDQuest("description.requirement.quest"), // 0: quest

	RWDTitle("description.reward.title"),

	/* Misc */

	TimeWeeks("misc.time.weeks"),
	TimeDays("misc.time.days"),
	TimeHours("misc.time.hours"),
	TimeMinutes("misc.time.minutes"),
	TimeLessMinute("misc.time.lessThanAMinute"),

	Find("misc.stageType.region"),
	Talk("misc.stageType.npc"),
	Items("misc.stageType.items"),
	Mobs("misc.stageType.mobs"),
	Mine("misc.stageType.mine"),
	Place("misc.stageType.placeBlocks"),
	Chat("misc.stageType.chat"),
	InteractBlock("misc.stageType.interact"),
	InteractLocation("misc.stageType.interactLocation"),
	Fish("misc.stageType.Fish"),
	Melt("misc.stageType.Melt"),
	Enchant("misc.stageType.Enchant"),
	Craft("misc.stageType.Craft"),
	Bucket("misc.stageType.Bucket"),
	StageLocation("misc.stageType.location"),
	PlayTime("misc.stageType.playTime"),
	Breed("misc.stageType.breedAnimals"),
	Tame("misc.stageType.tameAnimals"),
	Death("misc.stageType.die"),
	DealDamage("misc.stageType.dealDamage"),
	EatDrink("misc.stageType.eatDrink"),

	ComparisonEquals("misc.comparison.equals"),
	ComparisonDifferent("misc.comparison.different"),
	ComparisonLess("misc.comparison.less"),
	ComparisonLessOrEquals("misc.comparison.lessOrEquals"),
	ComparisonGreater("misc.comparison.greater"),
	ComparisonGreaterOrEquals("misc.comparison.greaterOrEquals"),

	RLOR("misc.requirement.logicalOr"),
	RClass("misc.requirement.class"),
	RSkillAPILevel("misc.requirement.skillAPILevel"),
	RFaction("misc.requirement.faction"),
	RJobLvl("misc.requirement.jobLevel"),
	RCombatLvl("misc.requirement.combatLevel"),
	RLevel("misc.requirement.experienceLevel"),
	RPermissions("misc.requirement.permissions"),
	RScoreboard("misc.requirement.scoreboard"),
	RRegion("misc.requirement.region"),
	RPlaceholder("misc.requirement.placeholder"),
	RQuest("misc.requirement.quest"),
	RSkillLvl("misc.requirement.mcMMOSkillLevel"),
	RMoney("misc.requirement.money"),
	REquipment("misc.requirement.equipment"),

	RWSkillApiXp("misc.reward.skillApiXp"),

	BucketWater("misc.bucket.water"),
	BucketLava("misc.bucket.lava"),
	BucketMilk("misc.bucket.milk"),
	BucketSnow("misc.bucket.snow"),

	ClickRight("misc.click.right"),
	ClickLeft("misc.click.left"),
	ClickShiftRight("misc.click.shift-right"),
	ClickShiftLeft("misc.click.shift-left"),
	ClickMiddle("misc.click.middle"),

	AmountItems("misc.amounts.items"), // 0: amount
	AmountComparisons("misc.amounts.comparisons"), // 0: amount
	AmountDialogLines("misc.amounts.dialogLines"), // 0: amount
	AmountPermissions("misc.amounts.permissions"), // 0: amount
	AmountMobs("misc.amounts.mobs"), // 0: amount
	AmountXp("misc.amounts.xp"),

	HologramText("misc.hologramText"),
	PoolHologramText("misc.poolHologramText"),
	EntityType("misc.entityType"),
	EntityTypeAny("misc.entityTypeAny"),
	QuestItemLore("misc.questItemLore"),
	Ticks("misc.ticks"),
	Location("misc.location"),
	Enabled("misc.enabled"),
	Disabled("misc.disabled"),
	Unknown("misc.unknown"),
	NotSet("misc.notSet"),
	Remove("misc.removeRaw"),
	Reset("misc.reset"),
	Or("misc.or"),
	Amount("misc.amount"),
	Yes("misc.yes"),
	No("misc.no"),
	And("misc.and");

	private static final String DEFAULT_STRING = "§cnot loaded";

	private final @NotNull String path;
	private final @NotNull MessageType type;
	private final @Nullable Lang prefix;

	private @NotNull String value = DEFAULT_STRING;


	private Lang(@NotNull String path) {
		this(path, MessageType.DefaultMessageType.PREFIXED, null);
	}

	private Lang(@NotNull String path, @NotNull MessageType type) {
		this(path, type, null);
	}

	private Lang(@NotNull String path, @Nullable Lang prefix) {
		this(path, MessageType.DefaultMessageType.PREFIXED, prefix);
	}

	private Lang(@NotNull String path, @NotNull MessageType type, @Nullable Lang prefix) {
		this.path = path;
		this.type = type;
		this.prefix = prefix;
	}

	@Override
	public @NotNull String getPath() {
		return path;
	}

	@Override
	public @NotNull MessageType getType() {
		return type;
	}

	@Override
	public void setValue(@NotNull String value) {
		this.value = Objects.requireNonNull(value);
	}

	@Override
	public @NotNull String getValue() {
		return prefix == null ? value : (prefix.toString() + value);
	}

	@Override
	public String toString() {
		return getValue();
	}

}
