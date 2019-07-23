package fr.skytasul.quests.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;


/**
 * Stores all string paths and methods to format and send them to players.
 */
public enum Lang{
	
	/* Messages */
	FINISHED_BASE("msg.quest.finished.base"),
	FINISHED_OBTAIN("msg.quest.finished.obtain"),
	STARTED_QUEST("msg.quest.started"),
	SUCCESFULLY_CREATED("msg.quest.created"),
	SUCCESFULLY_EDITED("msg.quest.edited"),
	CANCELLED("msg.quest.createCancelled"),
	QUEST_CANCEL("msg.quest.cancelling"),
	QUEST_EDIT_CANCEL("msg.quest.editCancelling"),
	QUEST_INVALID("msg.quest.invalidID"),
	ALREADY_STARTED("msg.quest.alreadyStarted"),
	
	QUEST_NOSTEPS("msg.quests.nopStep"),
	QUEST_UPDATED("msg.quests.updated"),
	
	STAGE_NOMOBS("msg.stageMobs.noMobs"),
	STAGE_MOBSLIST("msg.stageMobs.listMobs"),
	
	TYPE_CANCEL("msg.typeCancel"),
	NPC_TEXT("msg.writeNPCText"),
	REGION_NAME("msg.writeRegionName"),
	XP_GAIN("msg.writeXPGain"),
	MOB_AMOUNT("msg.writeMobAmount"),
	CHAT_MESSAGE("msg.writeChatMessage"),
	END_MESSAGE("msg.writeEndMessage"),
	DESC_MESSAGE("msg.writeDescriptionText"),
	START_TEXT("msg.writeStageText"),
	MOVE_TELEPORT_POINT("msg.moveToTeleportPoint"),
	NPC_NAME("msg.writeNpcName"), 
	NPC_SKIN("msg.writeNpcSkinName"), 
	QUEST_NAME("msg.writeQuestName"),
	COMMAND("msg.writeCommand"),
	HOLOGRAM_TEXT("msg.writeHologramText"),
	TIMER("msg.writeQuestTimer"),
	CONFIRM_MESSAGE("msg.writeConfirmMessage"),
	
	REQUIREMENT_QUEST("msg.requirements.quest"),
	REQUIREMENT_LEVEL("msg.requirements.level"),
	REQUIREMENT_JOB("msg.requirements.job"),
	REQUIREMENT_SKILL("msg.requirements.skill"),
	REQUIREMENT_COMBAT_LEVEL("msg.requirements.combatLevel"),
	QUEST_WAIT("msg.requirements.wait"),
	
	XP_EDITED("msg.experience.edited"),
	SELECT_KILL_NPC("msg.selectNPCToKill"),
	
	NPC_REMOVE("msg.npc.remove"),
	TALK_NPC("msg.npc.talk"),
	
	REGION_DOESNT_EXIST("msg.regionDoesntExists"),
	FACTION_DOESNT_EXIST("msg.factionDoesntExist"),
	NPC_DOESNT_EXIST("msg.npcDoesntExist"),
	CLASS_DOESNT_EXIST("msg.classDoesntExist"),
	OBJECT_DOESNT_EXIST("msg.objectDoesntExist"),
	NUMBER_NEGATIVE("msg.number.negative"),
	NUMBER_ZERO("msg.number.zero"),
	NUMBER_INVALID("msg.number.invalid"),
	ERROR_OCCURED("msg.errorOccurred"),
	CANT_COMMAND("msg.commandsDisabled"),
	OUT_OF_BOUNDS("msg.indexOutOfBounds"),
	
	NEED_OBJECTS("msg.bringBackObjects"),
	ITEM_DROPPED("msg.inventoryFull"),
	
	PLAYER_NEVER_CONNECTED("msg.playerNeverConnected"),
	PLAYER_NOT_ONLINE("msg.playerNotOnline"),
	
	MUST_PLAYER("msg.command.playerNeeded"),
	INCORRECT_SYNTAX("msg.command.incorrectSyntax"),
	PERMISSION_REQUIRED("msg.command.noPermission"),
	COMMAND_DOESNT_EXIST("msg.command.invalidCommand.quests"),
	COMMAND_DOESNT_EXIST_NOSLASH("msg.command.invalidCommand.simple"),
	MUST_HOLD_ITEM("msg.command.needItem"),
	ITEM_CHANGED("msg.command.itemChanged"),
	ITEM_REMOVED("msg.command.itemRemoved"),
	SUCCESFULLY_REMOVED("msg.command.removed"),
	LEAVE_ALL_RESULT("msg.command.leaveAll"),
	DATA_REMOVED("msg.command.resetPlayer.player"),
	DATA_REMOVED_INFO("msg.command.resetPlayer.remover"),
	DATA_QUEST_REMOVED("msg.command.resetPlayerQuest.player"),
	DATA_QUEST_REMOVED_INFO("msg.command.resetPlayerQuest.remover"),
	START_QUEST("msg.command.startQuest"),
	CANCEL_QUEST("msg.command.cancelQuest"),
	CANCEL_QUEST_UNAVAILABLE("msg.command.cancelQuestUnavailable"),
	BACKUP_CREATED("msg.command.backupCreated"),
	BACKUP_PLAYERS_FAILED("msg.command.backupPlayersFailed"),
	BACKUP_QUESTS_FAILED("msg.command.backupQuestsFailed"),
	ADMIN_MODE_ENTERED("msg.command.adminModeEntered"),
	ADMIN_MODE_LEFT("msg.command.adminModeLeft"),
	COMMAND_SCOREBOARD_LINESET("msg.command.scoreboard.lineSet"), // 0: line id
	COMMAND_SCOREBOARD_LINERESET("msg.command.scoreboard.lineReset"), // 0: line id
	COMMAND_SCOREBOARD_LINEREMOVE("msg.command.scoreboard.lineRemoved"), // 0: line id
	COMMAND_SCOREBOARD_LINENOEXIST("msg.command.scoreboard.lineInexistant"), // 0: line id
	COMMAND_SCOREBOARD_RESETALL("msg.command.scoreboard.resetAll"), // 0: player
	COMMAND_SCOREBOARD_HIDDEN("msg.command.scoreboard.hidden"), // 0: player
	COMMAND_SCOREBOARD_SHOWN("msg.command.scoreboard.shown"), // 0: player
	COMMAND_SETSTAGE_BRANCH_DOESNTEXIST("msg.command.setStage.branchDoesntExist"),
	COMMAND_SETSTAGE_STAGE_DOESNTEXIST("msg.command.setStage.doesntExist"),
	COMMAND_SETSTAGE_NEXT("msg.command.setStage.next"),
	COMMAND_SETSTAGE_NEXT_UNAVAILABLE("msg.command.setStage.nextUnavailable"),
	COMMAND_SETSTAGE_SET("msg.command.setStage.set"),
	
	COMMAND_HELP("msg.command.help.header"),
	COMMAND_HELP_CREATE("msg.command.help.create"),
	COMMAND_HELP_EDIT("msg.command.help.edit"),
	COMMAND_HELP_REMOVE("msg.command.help.remove"),
	COMMAND_HELP_FINISH("msg.command.help.finishAll"),
	COMMAND_HELP_STAGE("msg.command.help.setStage"),
	COMMAND_HELP_RESET("msg.command.help.resetPlayer"),
	COMMAND_HELP_RESETQUEST("msg.command.help.resetPlayerQuest"),
	COMMAND_HELP_SEE("msg.command.help.seePlayer"),
	COMMAND_HELP_RELOAD("msg.command.help.reload"),
	COMMAND_HELP_START("msg.command.help.start"),
	COMMAND_HELP_SETITEM("msg.command.help.setItem"),
	COMMAND_HELP_ADMINMODE("msg.command.help.adminMode"),
	COMMAND_HELP_VERSION("msg.command.help.version"),
	COMMAND_HELP_SAVE("msg.command.help.save"),
	COMMAND_HELP_LIST("msg.command.help.list"),
	
	// * Editors *
	ALREADY_EDITOR("msg.editor.already"),
	ENTER_EDITOR_TITLE("msg.editor.enter.title"),
	ENTER_EDITOR_SUB("msg.editor.enter.subtitle"),
	CHAT_EDITOR("msg.editor.chat"),
	NPC_EDITOR_ENTER("msg.editor.npc.enter"),

	ARG_NOT_SUPPORTED("msg.editor.text.argNotSupported"),

	CHOOSE_NPC_STARTER("msg.editor.npc.choseStarter"),
	NPC_NOT_QUEST("msg.editor.npc.notStarter"),
	
	CLICK_BLOCK("msg.editor.selectWantedBlock"),
	BLOCKS_AMOUNT("msg.editor.blocksToMineAmount"),
	BLOCKS_NAME("msg.editor.blocksToMineName"),
	
	BUCKET_AMOUNT("msg.editor.typeBucketAmount"),
	
	LOCATION_GO("msg.editor.goToLocation"),
	LOCATION_RADIUS("msg.editor.typeLocationRadius"),
	
	// requirements
	CHOOSE_XP_REQUIRED("msg.editor.text.chooseLvlRequired"),
	CHOOSE_JOB_REQUIRED("msg.editor.text.chooseJobRequired"),
	CHOOSE_PERM_REQUIRED("msg.editor.text.choosePermissionRequired"),
	CHOOSE_PERM_REQUIRED_MESSAGE("msg.editor.text.choosePermissionMessage"),
	CHOOSE_FAC_REQUIRED("msg.editor.text.chooseFactionRequired"),
	CHOOSE_CLASSES_REQUIRED("msg.editor.text.chooseClassesRequired"),
	CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER("msg.editor.text.choosePlaceholderRequired.identifier"),
	CHOOSE_PLACEHOLDER_REQUIRED_VALUE("msg.editor.text.choosePlaceholderRequired.value"),
	CHOOSE_SKILL_REQUIRED("msg.editor.text.chooseSkillRequired"),
	// rewards
	CHOOSE_PERM_REWARD("msg.editor.text.reward.permission"),
	CHOOSE_MONEY_REWARD("msg.editor.text.reward.money"),

	CHOOSE_ITEM_TYPE("msg.editor.itemCreator.itemType"),
	CHOOSE_ITEM_NAME("msg.editor.itemCreator.itemName"),
	CHOOSE_ITEM_LORE("msg.editor.itemCreator.itemLore"),
	UNKNOWN_ITEM_TYPE("msg.editor.itemCreator.unknownItemType"),
	INVALID_ITEM_TYPE("msg.editor.itemCreator.invalidItemType"),
	
	DIALOG_SYNTAX("msg.editor.dialog.syntax"),
	DIALOG_REMOVE_SYNTAX("msg.editor.dialog.syntaxRemove"),
	DIALOG_MSG_ADDED_PLAYER("msg.editor.dialog.player"),
	DIALOG_MSG_ADDED_NPC("msg.editor.dialog.npc"),
	DIALOG_MSG_ADDED_NOSENDER("msg.editor.dialog.noSender"),
	DIALOG_MSG_REMOVED("msg.editor.dialog.messageRemoved"),
	DIALOG_SOUND_ADDED("msg.editor.dialog.soundAdded"),
	DIALOG_CLEARED("msg.editor.dialog.cleared"),
	DIALOG_HELP_HEADER("msg.editor.dialog.help.header"),
	DIALOG_HELP_NPC("msg.editor.dialog.help.npc"),
	DIALOG_HELP_PLAYER("msg.editor.dialog.help.player"),
	DIALOG_HELP_NOTHING("msg.editor.dialog.help.nothing"),
	DIALOG_HELP_REMOVE("msg.editor.dialog.help.remove"),
	DIALOG_HELP_LIST("msg.editor.dialog.help.list"),
	DIALOG_HELP_NPCINSERT("msg.editor.dialog.help.npcInsert"),
	DIALOG_HELP_PLAYERINSERT("msg.editor.dialog.help.playerInsert"),
	DIALOG_HELP_NOTHINGINSERT("msg.editor.dialog.help.nothingInsert"),
	DIALOG_HELP_ADDSOUND("msg.editor.dialog.help.addSound"),
	DIALOG_HELP_CLEAR("msg.editor.dialog.help.clear"),
	DIALOG_HELP_CLOSE("msg.editor.dialog.help.close"),
	
	MYTHICMOB_LIST("msg.editor.mythicmobs.list"),
	MYTHICMOB_NOT_EXISTS("msg.editor.mythicmobs.isntMythicMob"),
	MYTHICMOB_DISABLED("msg.editor.mythicmobs.disabled"),
	EPICBOSS_NOT_EXISTS("msg.editor.epicBossDoesntExist"),
	
	TEXTLIST_SYNTAX("msg.editor.textList.syntax"),
	TEXTLIST_TEXT_ADDED("msg.editor.textList.added"),
	TEXTLIST_TEXT_REMOVED("msg.editor.textList.removed"),
	TEXTLIST_TEXT_HELP_HEADER("msg.editor.textList.help.header"),
	TEXTLIST_TEXT_HELP_ADD("msg.editor.textList.help.add"),
	TEXTLIST_TEXT_HELP_REMOVE("msg.editor.textList.help.remove"),
	TEXTLIST_TEXT_HELP_LIST("msg.editor.textList.help.list"),
	TEXTLIST_TEXT_HELP_CLOSE("msg.editor.textList.help.close"),
	
	// * Quests lists*

	Finished("advancement.finished"),
	Not_Started("advancement.notStarted"),
	
	/* Inventories */
	done("inv.validate"),
	cancel("inv.cancel"),
	
	INVENTORY_CONFIRM("inv.confirm.name"),
	confirmYes("inv.confirm.yes"),
	confirmNo("inv.confirm.no"),
	
	stageCreate("inv.create.stageCreate"),
	stageRemove("inv.create.stageRemove"),
	stageNPC("inv.create.findNPC"),
	stageBring("inv.create.bringBack"),
	stageGoTo("inv.create.findRegion"),
	stageMobs("inv.create.killMobs"),
	stageMine("inv.create.mineBlocks"),
	stageChat("inv.create.talkChat"),
	stageInteract("inv.create.interact"),
	stageFish("inv.create.fish"),
	stageCraft("inv.create.craft"),
	stageBucket("inv.create.bucket"),
	stageLocation("inv.create.location"),
	stageText("inv.create.NPCText"),
	stageHide("inv.create.hideClues"),
	editMobs("inv.create.editMobsKill"),
	mobsKillType("inv.create.mobsKillFromAFar"),
	editBlocks("inv.create.editBlocksMine"),
	preventBlockPlace("inv.create.preventBlockPlace"),
	editMessage("inv.create.editMessageType"),
	cancelEvent("inv.create.cancelMessage"),
	stageItems("inv.create.selectItems"),
	stageRegion("inv.create.selectRegion"),
	startMsg("inv.create.stageStartMsg"),
	blockLocation("inv.create.selectBlockLocation"),
	leftClick("inv.create.leftClick"),
	editFishes("inv.create.editFishes"),
	editItem("inv.create.editItem"),
	editBucketType("inv.create.editBucketType"),
	editBucketAmount("inv.create.editBucketAmount"),
	editLocation("inv.create.editLocation"),
	editRadius("inv.create.editRadius"),
	currentRadius("inv.create.currentRadius"), // 0: radius
	
	INVENTORY_STAGES("inv.stages.name"),
	nextPage("inv.stages.nextPage"),
	laterPage("inv.stages.laterPage"),
	regularPage("inv.stages.regularPage"),
	branchesPage("inv.stages.branchesPage"),
	ending("inv.stages.endingItem"),
	descMessage("inv.stages.descriptionTextItem"),
	previousBranch("inv.stages.previousBranch"),
	newBranch("inv.stages.newBranch"),
	
	INVENTORY_DETAILS("inv.details.name"),
	multiple("inv.details.multipleTime.itemName"),
	multipleLore("inv.details.multipleTime.itemLore"),
	cancellable("inv.details.cancellable"),
	scoreboard("inv.details.scoreboardItem"),
	hide("inv.details.hideItem"),
	bypass("inv.details.bypassLimit"),
	questName("inv.details.questName"),
	rewardItems("inv.details.setItemsRewards"),
	rewardXP("inv.details.setXPRewards"),
	rewardPerm("inv.details.setPermReward"),
	rewardMoney("inv.details.setMoneyReward"),
	questStarterCreate("inv.details.createStarterNPC"),
	questStarterSelect("inv.details.selectStarterNPC"),
	create("inv.details.createQuest.itemName"),
	createLore("inv.details.createQuest.itemLore"),
	edit("inv.details.editQuest.itemName"),
	endMessage("inv.details.endMessage"),
	startDialog("inv.details.startDialog"),
	editRequirements("inv.details.editRequirements"),
	startRewards("inv.details.startRewards"),
	hologramText("inv.details.hologramText"),
	timer("inv.details.timer"),
	requirements("inv.details.requirements"),
	rewards("inv.details.rewards"),
	hologramLaunch("inv.details.hologramLaunch"),
	hologramLaunchNo("inv.details.hologramLaunchNo"),
	customConfirmMessage("inv.details.customConfirmMessage"),
	
	INVENTORY_ITEMS("inv.itemsSelect.name"),
	itemsNone("inv.itemsSelect.none"),
	
	INVENTORY_ITEM("inv.itemSelect.name"),
	
	INVENTORY_NPC("inv.npcCreate.name"),
	name("inv.npcCreate.setName"),
	skin("inv.npcCreate.setSkin"),
	type("inv.npcCreate.setType"),
	move("inv.npcCreate.move.itemName"),
	moveLore("inv.npcCreate.move.itemLore"),
	moveItem("inv.npcCreate.moveItem"),

	INVENTORY_SELECT("inv.npcSelect.name"),
	selectNPC("inv.npcSelect.selectStageNPC"),
	createNPC("inv.npcSelect.createStageNPC"),
	
	INVENTORY_TYPE("inv.entityType.name"),
	INVENTORY_CHOOSE("inv.chooseQuest.name"),
	
	INVENTORY_MOBS("inv.mobs.name"),
	mobsNone("inv.mobs.none"),
	click("inv.mobs.click"),

	INVENTORY_MOBSELECT("inv.mobSelect.name"),
	bukkitMob("inv.mobSelect.bukkitEntityType"),
	mythicMob("inv.mobSelect.mythicMob"),
	epicBoss("inv.mobSelect.epicBoss"),
	
	location("inv.stageEnding.locationTeleport"),
	command("inv.stageEnding.command"),
	
	INVENTORY_REQUIREMENTS("inv.requirements.name"),
	
	INVENTORY_REWARDS("inv.rewards.name"),
	commands("inv.rewards.commands"),
	teleportation("inv.rewards.teleportation"),

	INVENTORY_QUESTS_LIST("inv.listAllQuests.name"),
	INVENTORY_PLAYER_LIST("inv.listPlayerQuests.name"),
	notStarteds("inv.listQuests.notStarted"),
	finisheds("inv.listQuests.finished"),
	inProgress("inv.listQuests.inProgress"),
	cancelLore("inv.listQuests.loreCancel"),
	timeWait("inv.listQuests.timeToWait"),
	canRedo("inv.listQuests.canRedo"),
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
	
	INVENTORY_COMMANDS_LIST("inv.commandsList.name"),
	commandsListValue("inv.commandsList.value"),
	commandsListConsole("inv.commandsList.console"),
	
	INVENTORY_CHOOSEACCOUNT("inv.chooseAccount.name"),
	
	INVENTORY_BLOCK("inv.block.name"),
	materialName("inv.block.material"),
	
	INVENTORY_BLOCKSLIST("inv.blocksList.name"),
	addBlock("inv.blocksList.addBlock"),
	
	INVENTORY_BUCKETS("inv.buckets.name"),
	
	
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
	SCOREBOARD_REG("scoreboard.stage.region"),
	SCOREBOARD_NPC("scoreboard.stage.npc"),
	SCOREBOARD_ITEMS("scoreboard.stage.items"),
	SCOREBOARD_MOBS("scoreboard.stage.mobs"),
	SCOREBOARD_MINE("scoreboard.stage.mine"),
	SCOREBOARD_CHAT("scoreboard.stage.chat"),
	SCOREBOARD_INTERACT("scoreboard.stage.interact"),
	SCOREBOARD_FISH("scoreboard.stage.fish"),
	SCOREBOARD_CRAFT("scoreboard.stage.craft"),
	SCOREBOARD_BUCKET("scoreboard.stage.bucket"),
	SCOREBOARD_LOCATION("scoreboard.stage.location"), // 0: x, 1: y, 2: z, 3: world
	
	/* Indications */
	
	INDICATION_START("indication.startQuest"), // 0: quest name
	INDICATION_CLOSE("indication.closeInventory"),
	INDICATION_CANCEL("indication.cancelQuest"), // 0: quest name
	INDICATION_REMOVE("indication.removeQuest"), // 0: quest name
	
	/* Misc */
	
	Prefix("misc.format.prefix"),
	NpcText("misc.format.npcText"), // 0: npc, 1: msg, 2: index, 3: max
	SelfText("misc.format.selfText"), // 0: player, 1: msg, 2: index, 3: max
	OffText("misc.format.offText"), // 1: msg
	
	Find("misc.stageType.region"),
	Talk("misc.stageType.npc"),
	Items("misc.stageType.items"),
	Mobs("misc.stageType.mobs"),
	Mine("misc.stageType.mine"),
	Chat("misc.stageType.chat"),
	Interact("misc.stageType.interact"),
	Fish("misc.stageType.Fish"),
	Craft("misc.stageType.Craft"),
	Bucket("misc.stageType.Bucket"),
	Location("misc.stageType.location"),
	
	RClass("misc.requirement.class"),
	RFaction("misc.requirement.faction"),
	RJobLvl("misc.requirement.jobLevel"),
	RCombatLvl("misc.requirement.combatLevel"),
	RLevel("misc.requirement.experienceLevel"),
	RPermissions("misc.requirement.permissions"),
	RPlaceholder("misc.requirement.placeholder"),
	RQuest("misc.requirement.quest"),
	RSkillLvl("misc.requirement.mcMMOSkillLevel"),
	BucketWater("misc.bucket.water"),
	BucketLava("misc.bucket.lava"),
	BucketMilk("misc.bucket.milk"),
	HologramText("misc.hologramText"),
	MobsProgression("misc.mobsProgression"),
	EntityType("misc.entityType"),
	QuestItemLore("misc.questItemLore"),
	Enabled("misc.enabled"),
	Disabled("misc.disabled"),
	Unknown("misc.unknown"),
	Unused("misc.unused"),
	Used("misc.used"),
	Remove("misc.remove"),
	Or("misc.or"),
	Amount("misc.amount"),
	Item("misc.items"),
	Exp("misc.expPoints"),
	Yes("misc.yes"),
	No("misc.no"),
	And("misc.and");
	
	
	private String path;
	private String value;

	private Lang(String path){
		this.path = path;
	}
	
	public String getPath(){
		return path;
	}
	
	private void setValue(String value){
		this.value = value;
	}
	
	public String toString(){
		return value;
	}
	
	public String format(Object... replace){
		return Utils.format(toString(), replace);
	}
	
	public void send(CommandSender sender, Object... args){
		Utils.sendMessage(sender, this.toString(), args);
	}
	
	public void sendWP(CommandSender p, Object... args){
		Utils.sendMessageWP(p, this.toString(), args);
	}


	
	public static void loadStrings(YamlConfiguration config) {
		for (Lang l : values()){
			String value = config.getString(l.path, null);
			if (value == null) DebugUtils.logMessage("Unavailable string in config for key " + l.path);
			l.setValue(ChatColor.translateAlternateColorCodes('&', value == null ? "§cunknown string" : value));
		}
	}
	
}