package fr.skytasul.quests.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang{
	
	/* Messages */
	FINISHED_BASE("msg.quest.finished.base", "§aCongratulations! You have finished the quest §o§6{0}§r§a!" /*{1} points d'expérience et {2} items !"*/),
	FINISHED_OBTAIN("msg.quest.finished.obtain", "§aYou obtain {0}!"),
	STARTED_QUEST("msg.quest.started", "You start the quest §o{0}"),
	SUCCESFULLY_CREATED("msg.quest.created", "§aCongratulations! You have created the quest \"§e{0}§a\", who includes {1} steps!"),
	SUCCESFULLY_EDITED("msg.quest.edited", "§aCongratulations! You have edited the quest \"§e{0}§a\", who includes now {1} steps!"),
	CANCELLED("msg.quest.createCancelled", "§cThe creation (or the edition) has been cancelled by another plugin, sorry..."),
	QUEST_CANCEL("msg.quest.cancelling", "§cProcess of quest creation cancelled."),
	QUEST_EDIT_CANCEL("msg.quest.editCancelling", "§cProcess of quest edition cancelled."),
	QUEST_INVALID("msg.quest.invalidID", "§cThe quests with ID {0} does not exist."),
	
	QUEST_NOSTEPS("msg.quests.nopStep", "§cThis quest doesn't have any step... prevent an administrator :-)"),
	QUEST_UPDATED("msg.quests.updated", "§7Quest {0} updated!"),
	
	STAGE_NOMOBS("msg.stageMobs.noMobs", "§cThis stage doesn't need any mobs to kill... prevent an administrator :-)"),
	STAGE_MOBSLIST("msg.stageMobs.listMobs", "§aYou must kill {0}."),
	
	TRICK_TEXT("msg.trick.text",  "Type {0} or {1}"),
	TRICK_CANCEL("msg.trick.cancel", "\"cancel\" to come back at the last text"),
	TRICK_NULL("msg.trick.nul", "\"null\" to say nothing"),
	NPC_TEXT("msg.writeNPCText", "Write the dialog will be said to the player by the NPC. Type \\\"help\\\" to get some help. (all commands are without /)"),
	REGION_NAME("msg.writeRegionName", "Write the name of the region required for the step."),
	XP_GAIN("msg.writeXPGain", "Write the amount of experience points that the player will be obtain. Last gain: §a{0}"),
	MOB_AMOUNT("msg.writeMobAmount", "Write the amount of the mobs to kill."),
	CHAT_MESSAGE("msg.writeChatMessage", "Write requisite message (add {SLASH} at the beggining if you want a command)."),
	END_MESSAGE("msg.writeEndMessage", "Write the message sent at the end of the quest or at the end of the stage."),
	DESC_MESSAGE("msg.writeDescriptionText", "Write the text who describe the goal of the stage."),
	START_TEXT("msg.writeStageText", "Write the text will be sent to the player at the beggining of the step."),
	MOVE_TELEPORT_POINT("msg.moveToTeleportPoint", "Go to wanted teleport point."),
	NPC_NAME("msg.writeNpcName", "Write the name of the NPC."), 
	NPC_SKIN("msg.writeNpcSkinName", "Write the name of the skin of the NPC."), 
	QUEST_NAME("msg.writeQuestName", "Write the name of your quest!"),
	COMMAND("msg.writeCommand", "Write wanted command. (without / at the beggining) You can use {PLAYER}, it will be remplaced by the executor player's name."),
	HOLOGRAM_TEXT("msg.writeHologramText", "Write text of the hologram. (type \"none\" if you do not want a hologram, and \"null\" if you want default text)"),
	TIMER("msg.writeQuestTimer", "Write required time before you can restart the quest. (type \"null\" if you want default timer)"),
	
	REQUIREMENT_QUEST("msg.requirements.quest", "§eYou must have finished the quest \"{0}\"§r§e!"),
	REQUIREMENT_LEVEL("msg.requirements.level", "§eYou must be at least level {0}!"),
	REQUIREMENT_JOB("msg.requirements.job", "§eYou must be at least level {0} for the job {1}!"),
	REQUIREMENT_SKILL("msg.requirements.skill", "§eYou must be at least level {0} for the skill {1}!"),
	QUEST_WAIT("msg.requirements.wait", "§cYou must wait {0} minuts before starting this quest again!"),
	
	XP_EDITED("msg.experience.edited", "Experience gain edited: {0}pts. to {1}pts."),
	SELECT_KILL_NPC("msg.selectNPCToKill", "Select NPC to kill."),
	
	NPC_REMOVE("msg.npc.remove", "§7NPC deleted."),
	TALK_NPC("msg.npc.talk", "§7Go talk to a NPC named §o{0}§r§7."),
	
	REGION_DOESNT_EXIST("msg.regionDoesntExists", "§cThis region doesn't exists... (you must be in the same world that the wanted region)"),
	FACTION_DOESNT_EXIST("msg.factionDoesntExist", "§cThis faction does not exist..."),
	NPC_DOESNT_EXIST("msg.npcDoesntExist", "§cThe NPC with ID {0} does not exist."),
	CLASS_DOESNT_EXIST("msg.classDoesntExist", "§cThis class does not exist..."),
	OBJECT_DOESNT_EXIST("msg.objectDoesntExist", "§cSpecified item (ID {0}) does not exist."),
	NUMBER_NEGATIVE("msg.number.negative", "§cYou must enter a positive number!"),
	NUMBER_ZERO("msg.number.zero", "§cYou must enter a number other than 0!"),
	NUMBER_INVALID("msg.number.invalid", "§c\"{0}\" isn't an valid number."),
	ERROR_OCCURED("msg.errorOccured", "§cAn error occured... prevent an administrator :-)\n§4§lError code :{0}"),
	CANT_COMMAND("msg.commandsDisabled", "§cYou can't do command for this moment."),
	OUT_OF_BOUNDS("msg.indexOutOfBounds", "§cNumber specified ({0}) is out of bounds [{1}, {2}["),
	
	NEED_OBJECTS("msg.bringBackObjects", "Bring me back {0}."),
	ITEM_DROPPED("msg.inventoryFull", "§cYour inventory is full, the item has been dropped on floor."),
	
	PLAYER_NEVER_CONNECTED("msg.playerNeverConnected", "§cUnable to find datas from player {0}."),
	PLAYER_NOT_ONLINE("msg.playerNotOnline", "§cThe player {0} is not online."),
	
	MUST_PLAYER("msg.command.playerNeeded", "§cYou must be a player to do this command."),
	INCORRECT_SYNTAX("msg.command.incorrectSyntax", "§cIncorrect syntax."),
	//INCORRECT_ARGUMENT("msg.command.incorrectArgument", "§cInvalid argument : \"{0}\", expected : {1}"),
	PERMISSION_REQUIRED("msg.command.noPermission", "§cYou don't have the permission required to do this command. ({0})"),
	COMMAND_DOESNT_EXIST("msg.command.invalidCommand.quests", "§cThis command doesn't exist, type §o/quests help"),
	COMMAND_DOESNT_EXIST_NOSLASH("msg.command.invalidCommand.simple", "§cThis command doesn't exist, type §ohelp (without /)"),
	MUST_HOLD_ITEM("msg.command.needItem", "§cYou must hold an item in your main hand."),
	TRAIT_NAME("msg.command.traitNameChanged", "§aDisplay name of NPC has been edited."),
	ITEM_CHANGED("msg.command.itemChanged", "§aItem has been edited. The changement will be effective after restart."),
	SUCCESFULLY_REMOVED("msg.command.removed", "§aThe quest {0} has been successfully removed."),
	LEAVE_ALL_RESULT("msg.command.leaveAll", "§eYou have forced the end of {0} quests. §c{1} error(s)."),
	DATA_REMOVED("msg.command.resetPlayer.player", "§eAll datas of your quests ({0}) has been deleted by {1}."),
	DATA_QUEST_REMOVED("msg.command.resetPlayerQuest.player", "§eDatas of quest {0} has been deleted by {1}."),
	DATA_REMOVED_INFO("msg.command.resetPlayer.remover", "§eThe quests datas of {0} has been deleted. ({1})"),
	START_QUEST("msg.command.startQuest", "§eYou have forced starting of quest {0} (UUID {1})."),
	CANCEL_QUEST("msg.command.cancelQuest", "§eYou have cancelled the quest {0}."),
	SEE_PLAYER("msg.command.seePlayer.header", "§eDatas of player §6{0} §e"),
	SEE_PLAYER_INPROGRESS("msg.command.seePlayer.inProgress", "§eQuests in progress:"),
	SEE_PLAYER_FINISHED("msg.command.seePlayer.finished", "§eQuests finished:"),
	
	COMMAND_HELP("msg.command.help.header", "§e§lBeautyQuests - §6help§r"),
	COMMAND_HELP_CREATE("msg.command.help.create", "§e/{0} create: §acreate a quest"),
	COMMAND_HELP_EDIT("msg.command.help.edit", "§e/{0} quests edit: §aedit a quest"),
	COMMAND_HELP_REMOVE("msg.command.help.remove", "§e/{0} remove [id]: §adelete the quest with id [id] or by clicking on his NPC if not specified"),
	COMMAND_HELP_FINISH("msg.command.help.finishAll", "§e/{0} finishAll: §afinish all quests in progress"),
	COMMAND_HELP_STAGE("msg.command.help.setStage", "§e/{0} setStage <player> <quest> [newStage] : §afinish a stage for a player"),
	COMMAND_HELP_RESET("msg.command.help.resetPlayer", "§e/{0} resetPlayer <name>: §aremove all datas of a player"),
	COMMAND_HELP_RESETQUEST("msg.command.help.resetPlayerQuest", "§e/{0} resetPlayerQuest <name> [id]: §adelete datas of only one quest for a player"),
	COMMAND_HELP_SEE("msg.command.help.seePlayer", "§e/{0} seePlayer <name>: §asee datas of a player"),
	COMMAND_HELP_RELOAD("msg.command.help.reload", "§e/{0} reload: §asave and reload the configuration and data files - §c§odeprecated"),
	COMMAND_HELP_START("msg.command.help.start", "§e/{0} start <player> [id] : §aforce starting of a quest"),
	COMMAND_HELP_SETITEM("msg.command.help.setItem", "§e/{0} setItem <talk|launch> : §asave the hologram item in the data file"),
	COMMAND_HELP_VERSION("msg.command.help.version", "§e/{0} version: §asee the plugin version"),
	COMMAND_HELP_SAVE("msg.command.help.save", "§e/{0} save: §amake a manual save of datas"),
	COMMAND_HELP_LIST("msg.command.help.list", "§e/{0} list: §asee the quests list (for supported versions)"),
	
	// * Editors *
	ALREADY_EDITOR("msg.editor.already", "§cYou are already in an editor."),
	NPC_EDITOR_ENTER("msg.editor.npc.enter", "§aClick on a NPC, or type \"cancel\"."),

	ARG_NOT_SUPPORTED("msg.editor.text.argNotSupported", "§cArgument {0} not supported."),

	CHOOSE_NPC_STARTER("msg.editor.npc.choseStarter", "§3Choose the NPC who starts the quest."),
	NPC_NOT_QUEST("msg.editor.npc.notStarter", "§cThis NPC isn't a quest starter..."),
	
	CLICK_BLOCK("msg.editor.selectWantedBlock", "§aClick with the stick on the wanted block for the stage."),
	
	// requirements
	CHOOSE_XP_REQUIRED("msg.editor.text.chooseLvlRequired", "§eType the quantity of levels required."),
	CHOOSE_JOB_REQUIRED("msg.editor.text.chooseJobRequired", "§eType name of the wanted job."),
	CHOOSE_PERM_REQUIRED("msg.editor.text.choosePermissionRequired", "§eEnter required permissions to start the quest."),
	CHOOSE_PERM_REQUIRED_MESSAGE("msg.editor.text.choosePermissionMessage", "§eYou can choose a rejection message if the player does not have the required permission. (to skip this step, type \"null\" and no message will be sent)"),
	CHOOSE_FAC_REQUIRED("msg.editor.text.chooseFactionRequired", "§eEnter required faction's name."),
	CHOOSE_CLASSES_REQUIRED("msg.editor.text.chooseClassesRequired", "§eEnter names of required classes to start the quest."),
	CHOOSE_PLACEHOLDER_REQUIRED_IDENTIFIER("msg.editor.text.choosePlaceholderRequired.identifier", "§eEnter the name of placeholder required (§lwithout %%§r§e)."),
	CHOOSE_PLACEHOLDER_REQUIRED_VALUE("msg.editor.text.choosePlaceholderRequired.value", "§eEnter required value of the placeholder §o%{0}%."),
	CHOOSE_SKILL_REQUIRED("msg.editor.text.chooseSkillRequired", "§eType name of the wanted skill."),
	// rewards
	CHOOSE_PERM_REWARD("msg.editor.text.reward.permission", "§eEdit gained or retired permissions."),
	CHOOSE_MONEY_REWARD("msg.editor.text.reward.money", "§eType the amount of money gained."),

	CHOOSE_ITEM_TYPE("msg.editor.itemCreator.itemType", "§eType the name of the wanted item type."),
	//CHOOSE_ITEM_DATA("msg.editor.itemCreator.itemData", "§eEntrez la donnée de l'item voulu. (0-15)"),
	CHOOSE_ITEM_NAME("msg.editor.itemCreator.itemName", "§eType item name."),
	CHOOSE_ITEM_LORE("msg.editor.itemCreator.itemLore", "§eModify the item lore. Type \"help\" to get some help."),
	UNKNOWN_ITEM_TYPE("msg.editor.itemCreator.unknownItemType", "§cUnknown item type."),
	INVALID_ITEM_TYPE("msg.editor.itemCreator.invalidItemType", "§cInvalid item type (it must be an item and not a block)."),
	
	DIALOG_SYNTAX("msg.editor.dialog.syntax", "§cCorrect syntax : {0}{1} <message>"),
	DIALOG_REMOVE_SYNTAX("msg.editor.dialog.syntaxRemove", "§cCorrect sytax: remove <id>"),
	DIALOG_MSG_ADDED("msg.editor.dialog.messageAdded", "§aMessage {0} added for the {1}."),
	DIALOG_MSG_REMOVED("msg.editor.dialog.messageRemoved", "§aMessage {0} removed."),
	DIALOG_SOUND_ADDED("msg.editor.dialog.soundAdded", "§aSound {0} added for message {1}."),
	DIALOG_CLEARED("msg.editor.dialog.cleared", "§a{0} removed messages."),
	DIALOG_HELP("msg.editor.dialog.help","§anpc <message> : add a message said by NPC\n"
					+ "player <message> : add a message said by player\n"
					+ "nothing <message> : add a message without any prefix\n"
					+ "remove <id> : remove a message\n"
					+ "list : view all messages\n"
					+ "npcinsert <id> <message> : insert a message said by NPC\n"
					+ "playerinsert <id> <message> : insert a message said by player\n"
					+ "nothinginsert <id> <message> : insert a message without any prefix\n"
					+ "addsound <id> <sound> : add a sound on message selected\n"
					+ "clear : remove all messages\n"
					+ "close : validate messages"),
	
	MYTHICMOB_LIST("msg.editor.mythicmobs.list", "§aList of all MythocMobs:"),
	MYTHICMOB_NOT_EXISTS("msg.editor.mythicmobs.isntMythicMob", "§cThis MythicMob doesn't exist."),
	MYTHICMOB_DISABLED("msg.editor.mythicmobs.disabled", "§cMythicMob is disabled."),
	EPICBOSS_NOT_EXISTS("msg.editor.epicBossDoesntExist", "§cThis EpicBoss doesn't exist."),
	
	TEXTLIST_SYNTAX("msg.editor.textList.syntax", "§cCorrect syntax: "),
	TEXTLIST_TEXT_ADDED("msg.editor.textList.added", "§aText {0} added."),
	TEXTLIST_TEXT_REMOVED("msg.editor.textList.removed", "§aText {0} removed."),
	
	// * Quests lists*

	Finished("advancement.finished", "Finished"),
	Not_Started("advancement.notStarted", "Not started"),
	
	/* Inventaires */
	done("inv.validate", "§b§lValidate"),
	cancel("inv.cancel", "§c§lCancel"),
	
	INVENTORY_CONFIRM("inv.confirm.name", "§8Are you sure?"),
	confirmYes("inv.confirm.yes", "§aConfirm"),
	confirmNo("inv.confirm.no", "§cCancel"),
	
	stageCreate("inv.create.stageCreate", "§aCreate a new step"),
	stageRemove("inv.create.stageRemove", "§cDelete this step"),
	stageNPC("inv.create.findNPC", "§aFind a NPC"),
	stageBring("inv.create.bringBack", "§aBring back items"),
	stageGoTo("inv.create.findRegion", "§aFind a region"),
	stageMobs("inv.create.killMobs", "§aKill mobs"),
	stageMine("inv.create.mineBlocks", "§aBreak blocks"),
	stageChat("inv.create.talkChat", "§aWrite in the chat"),
	stageInteract("inv.create.interact", "§aInteract with block"),
	stageText("inv.create.NPCText", "§eEdit the text of the NPC"),
	stageHide("inv.create.hideClues", "Hide indications (particles, holograms)"),
	editMobs("inv.create.editMobsKill", "§eEdit the mobs to kill"),
	mobsKillType("inv.create.mobsKillFromAFar", "Kill from a far"),
	editBlocks("inv.create.editBlocksMine", "§eEdit blocks to break"),
	editMessage("inv.create.editMessageType", "§eEdit message to write"),
	cancelEvent("inv.create.cancelMessage", "Cancel sending"),
	stageItems("inv.create.selectItems", "§bChoose the required items"),
	stageRegion("inv.create.selectRegion", "§7Choose the region"),
	startMsg("inv.create.stageStartMsg", "§eEdit the starting message"),
	blockLocation("inv.create.selectBlockLocation", "§6Select block location"),
	leftClick("inv.create.leftClick", "Left click"),
	
	INVENTORY_STAGES("inv.stages.name", "§8Create the stages"),
	nextPage("inv.stages.nextPage", "§eNext page"),
	laterPage("inv.stages.laterPage", "§ePrevious page"),
	ending("inv.stages.endingItem", "§aEnd rewards"),
	descMessage("inv.stages.descriptionTextItem", "§eEdit description text"),
	
	INVENTORY_DETAILS("inv.details.name", "§8Last details of quest"),
	multiple("inv.details.multipleTime.itemName", "Toggle the several-use"),
	multipleLore("inv.details.multipleTime.itemLore", "Is the quest can be done\\n several times?"),
	scoreboard("inv.details.scoreboardItem", "Enable scoreboard"),
	hide("inv.details.hideItem", "Hide quest (menu and dynmap)"),
	bypass("inv.details.bypassLimit", "Do not count quest limit"),
	questName("inv.details.questName", "§a§lEdit the name of the quest"),
	rewardItems("inv.details.setItemsRewards", "§7Edit items gains"),
	rewardXP("inv.details.setXPRewards", "§eEdit experience gains"),
	rewardPerm("inv.details.setPermReward", "§eEdit permissions"),
	rewardMoney("inv.details.setMoneyReward", "§eEdit the gain of money"),
	questStarterCreate("inv.details.createStarterNPC", "§e§lCreate the NPC starter"),
	questStarterSelect("inv.details.selectStarterNPC", "§6§lSelect the NPC starter"),
	create("inv.details.createQuest.itemName", "§lCreate the quest"),
	createLore("inv.details.createQuest.itemLore", "Must one NPC selected/created\\nAnd have chosen a name for the quest"),
	edit("inv.details.editQuest.itemName", "§lEdit the quest"),
	/*lvlRequired("inv.details.lvlRequired", "§aModifier le nombre de niveaux requis"),
	quRequired("inv.details.questRequired", "§aModifier la quête requise"),*/
	endMessage("inv.details.endMessage", "§aEdit the end message"),
	startDialog("inv.details.startDialog", "§eEdit the start dialog"),
	editRequirements("inv.details.editRequirements", "§eEdit requirements"),
	startRewards("inv.details.startRewards", "§6Start rewards"),
	hologramText("inv.details.hologramText", "§eHologram text"),
	timer("inv.details.timer", "§bTime before you can restart the quest"),
	requirements("inv.details.requirements", "{0} requirements"),
	rewards("inv.details.rewards", "{0} rewards"),
	
	INVENTORY_ITEMS("inv.itemsSelect.name", "§8Edit the items"),
	itemsNone("inv.itemsSelect.none", "§aMove an item here or\\n§aclick to open item editor"),
	
	INVENTORY_NPC("inv.npcCreate.name", "§5Create your own NPC!"),
	name("inv.npcCreate.setName", "§aEdit the name of the NPC"),
	skin("inv.npcCreate.setSkin", "§eEdit the skin of the NPC"),
	type("inv.npcCreate.setType", "§aEdit the type of the NPC"),
	move("inv.npcCreate.move.itemName", "§6Move"),
	moveLore("inv.npcCreate.move.itemLore", "Move to another place"),
	moveItem("inv.npcCreate.moveItem", "Validate the place"),

	INVENTORY_SELECT("inv.npcSelect.name", "§8Select or create?"),
	selectNPC("inv.npcSelect.selectStageNPC", "§6Select an existing NPC"),
	createNPC("inv.npcSelect.createStageNPC", "§eCreate the NPC"),
	
	INVENTORY_TYPE("inv.entityType.name", "§5Choose the type of entity"),
	INVENTORY_CHOOSE("inv.chooseQuest.name", "§5Which quest?"),
	
	INVENTORY_MOBS("inv.mobs.name", "§5Choose the mobs!"),
	mobsNone("inv.mobs.none", "§aClick here to add a mob"),
	click("inv.mobs.click", "§bLeft click to edit\\nRight click to delete"),

	INVENTORY_MOBSELECT("inv.mobSelect.name", "§8Select the mob"),
	bukkitMob("inv.mobSelect.bukkitEntityType", "§eSelect a type of entity"),
	mythicMob("inv.mobSelect.mythicMob", "§6Select a Mythic Mob"),
	epicBoss("inv.mobSelect.epicBoss", "§6Select an Epic Boss"),
	
	INVENTORY_STAGEENDING("inv.stageEnding.name", "§8End of the step"), // TODO check if useful (les 4)
	location("inv.stageEnding.locationTeleport", "§aEdit the teleport point"),
	skillexp("inv.stageEnding.skillExperience", "§aEdit SkillAPI experience gain"),
	command("inv.stageEnding.command", "§aEdit executed command"),
	
	INVENTORY_REQUIREMENTS("inv.requirements.name", "§8Requirements"),
	
	INVENTORY_REWARDS("inv.rewards.name", "§8Rewards"),

	INVENTORY_QUESTS_LIST("inv.listAllQuests.name", "§8Quests"),
	INVENTORY_PLAYER_LIST("inv.listPlayerQuests.name", "§8{0}'s Quests"),
	notStarteds("inv.listQuests.notStarted", "Not started Quests"),
	finisheds("inv.listQuests.finished", "Finished Quests"),
	inProgress("inv.listQuests.inProgress", "Quests in progress"),
	cancelLore("inv.listQuests.loreCancel", "§c§oClick to cancel the quest"),
	timeWait("inv.listQuests.timeToWait", "§3§o{0} minuts before you can restart this quest"),
	canRedo("inv.listQuests.canRedo", "§3§oYou can restart this quest!"),
	
	INVENTORY_CREATOR("inv.itemCreator.name", "§8Item creator"),
	itemType("inv.itemCreator.itemType", "§bItem type"),
	itemFlags("inv.itemCreator.itemFlags", "Toggle item flags"),
	itemName("inv.itemCreator.itemName", "§bItem name"),
	itemLore("inv.itemCreator.itemLore", "§bItem lore"),
	itemQuest("inv.itemCreator.isQuestItem", "§bQuest item:"),
	
	INVENTORY_COMMAND("inv.command.name", "§8Command"),
	commandValue("inv.command.value", "§bCommand"),
	console("inv.command.console", "Console"),
	
	INVENTORY_CHOOSEACCOUNT("inv.chooseAccount.name", "§5What account?"),
	
	
	BOOK_NAME("inv.listBook.questName", "Name"),
	BOOK_STARTER("inv.listBook.questStarter", "Starter"),
	BOOK_REWARDS("inv.listBook.questRewards", "Rewards"),
	BOOK_SEVERAL("inv.listBook.questMultiple", "Several times"),
	/*BOOK_LVL("inv.listBook.questLvlRequired", "Niveau requis"),
	BOOK_QUEST_REQUIREMENT("inv.listBook.questRequired", "Quête requise"),*/
	BOOK_REQUIREMENTS("inv.listBook.requirements", "Requirements"),
	BOOK_STAGES("inv.listBook.questStages", "Stages"),
	BOOK_NOQUEST("inv.listBook.noQuests", "No quests has been previously created"),
	
	/* Scoreboard */
	
	SCOREBOARD_NAME("scoreboard.name", "§6§lQuests"),
	SCOREBOARD_NONE("scoreboard.noLaunched", "§cNo quests in progress."),
	SCOREBOARD_REG("scoreboard.stage.region", "§eFind the region §6{0}"),
	SCOREBOARD_NPC("scoreboard.stage.npc", "§eTalk to the NPC §6{0}"),
	SCOREBOARD_ITEMS("scoreboard.stage.items", "§eFind items for §6{0} §e:"),
	SCOREBOARD_MOBS("scoreboard.stage.mobs", "§eKill §6{0}"),
	SCOREBOARD_MINE("scoreboard.stage.mine", "§eMine {0}"),
	SCOREBOARD_CHAT("scoreboard.stage.chat", "§eWrite §6{0}"),
	SCOREBOARD_INTERACT("scoreboard.stage.interact", "§eClick on the block at §6{0}"),
	
	/* Misc */
	
	Prefix("misc.format.prefix", "§6<§e§lQuests§r§6> §o"),
	NpcText("misc.format.npcText", "§6[{2}/{3}] §e§l{0}:§r§e {1}"),
	SelfText("misc.format.selfText", "§6[{2}/{3}] §e§l{0}:§r§e {1}"),
	OffText("misc.format.offText", "§r§e {0}"),
	Find("misc.stageType.region", "Find a region"),
	Talk("misc.stageType.npc", "Find a NPC"),
	Items("misc.stageType.items", "Bring back items"),
	Mobs("misc.stageType.mobs", "Kill mobs"),
	Mine("misc.stageType.mine", "Break blocks"),
	Chat("misc.stageType.chat", "Talk in the chat"),
	Interact("misc.stageType.interact", "Interact with block"),
	RClass("misc.requirement.class", "§bClass(es) required"),
	RFaction("misc.requirement.faction", "§bFaction(s) required"),
	RJobLvl("misc.requirement.jobLevel", "§bJob level required"),
	RLevel("misc.requirement.experienceLevel", "§bExperience level required"),
	RPermissions("misc.requirement.permissions", "§3Permission(s) requiered"),
	RPlaceholder("misc.requirement.placeholder", "§bPlaceholder value required"),
	RQuest("misc.requirement.quest", "§aQuest required"),
	RSkillLvl("misc.requirement.mcMMOSkillLevel", "§dSkill level required"),
	HologramText("misc.hologramText", "§8§lQuest NPC"),
	MobsProgression("misc.mobsProgression", "§6§l{0}: §r§e{1}/{2}"),
	Entity_Type("misc.entityType", "Type d'entité : {0}"),
	Enabled("misc.enabled", "Enabled"),
	Disabled("misc.disabled", "Disabled"),
	Unknown("misc.unknown", "unknown"),
	Unused("misc.unused", "§e§lUnused"),
	Used("misc.used", "§6§lUsed"),
	Remove("misc.remove", "§e§oMiddle click to remove"),
	Null("misc.nul", "null"),
	Or("misc.or", "or"),
	Amount("misc.amount", "Amount"),
	Type("misc.type", "Type"),
	Item("misc.items", "items"),
	Exp("misc.expPoints", "experience points"),
	Yes("misc.yes", "true"),
	No("misc.no", "no"),
	And("misc.and", "and");
	
	
	private String path;
	private String def;
	private static YamlConfiguration LANG;

	Lang(String path, String start) {
		this.path = path;
		this.def = start;
	}

	public String getDefault() {
		return this.def;
	}

	public String getPath() {
		return this.path;
	}

	
	public String toString() {
		return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def))/*.replaceAll("\\\n", "\\n")*/;
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


	
	public static void setFile(YamlConfiguration config) {
		LANG = config;
	}
	
}