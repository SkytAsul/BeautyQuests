package fr.skytasul.quests.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.Dialogable;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.QuestCreationSession;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.gui.misc.ListBook;
import fr.skytasul.quests.gui.pools.PoolsManageGUI;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.gui.quests.QuestsListGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerPoolDatas;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.players.PlayersManagerDB;
import fr.skytasul.quests.players.PlayersManagerYAML;
import fr.skytasul.quests.rewards.CheckpointReward;
import fr.skytasul.quests.scoreboards.Scoreboard;
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Database;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.nms.NMS;
import fr.skytasul.quests.utils.types.DialogRunner;

public class Commands {
	
	@Cmd (permission = "create", player = true, noEditorInventory = true)
	public void create(CommandContext cmd){
		QuestCreationSession session = new QuestCreationSession();
		if (cmd.args.length == 1) {
			Integer id = Utils.parseInt(cmd.sender, cmd.get(0));
			if (id == null) return;
			if (QuestsAPI.getQuests().getQuest(id) != null) {
				Utils.sendMessage(cmd.sender, "Invalid quest ID: another quest exists with ID {0}", id);
				return;
			}
			session.setCustomID(id);
		}
		session.openMainGUI(cmd.player);
	}
	
	@Cmd (permission = "edit", args = "QUESTSID", player = true, noEditorInventory = true)
	public void edit(CommandContext cmd){
		if (cmd.args.length >= 1) {
			new QuestCreationSession(cmd.get(0)).openMainGUI(cmd.player);
			return;
		}
		Lang.CHOOSE_NPC_STARTER.send(cmd.player);
		new SelectNPC(cmd.player, () -> {}, npc -> {
			if (npc == null) return;
			if (!npc.getQuests().isEmpty()) {
				Inventories.create(cmd.player, new ChooseQuestGUI(npc.getQuests(), quest -> {
					if (quest == null) return;
					new QuestCreationSession(quest).openMainGUI(cmd.player);
				}));
			}else {
				Lang.NPC_NOT_QUEST.send(cmd.player);
			}
		}).enter();
	}
	
	@Cmd(permission = "remove", args = "QUESTSID")
	public void remove(CommandContext cmd){	
		if (cmd.args.length >= 1){
			try{
				remove(cmd.sender, (Quest) cmd.args[0]);
			}catch (NumberFormatException ex){
				Lang.NUMBER_INVALID.send(cmd.sender, cmd.args[1]);
			}
			return;
		}else if (!cmd.isPlayer()){
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
			return;
		}
		Lang.CHOOSE_NPC_STARTER.send(cmd.sender);
		new SelectNPC(cmd.player, () -> {}, npc -> {
			if (npc == null) return;
			if (!npc.getQuests().isEmpty()) {
				Inventories.create(cmd.player, new ChooseQuestGUI(npc.getQuests(), quest -> {
					if (quest == null) return;
					remove(cmd.sender, quest);
				}));
			}else {
				Lang.NPC_NOT_QUEST.send(cmd.sender);
			}
		}).enter();
	}

	@Cmd (permission = "manage")
	public void reload(CommandContext cmd){
		BeautyQuests.getInstance().performReload(cmd.sender);
	}
	
	@Cmd(permission = "version")
	public void version(CommandContext cmd){
		cmd.sender.sendMessage("§eBeautyQuests version : §6§l" + BeautyQuests.getInstance().getDescription().getVersion());
	}
	
	@Cmd (permission = "manage")
	public void save(CommandContext cmd){
		try {
			BeautyQuests.getInstance().saveAllConfig(false);
			cmd.sender.sendMessage("§aDatas saved!");
			BeautyQuests.logger.info("Datas saved ~ manual save from " + cmd.sender.getName());
		} catch (Throwable e) {
			e.printStackTrace();
			cmd.sender.sendMessage("Error while saving the data file.");
		}
	}
	
	@Cmd(permission = "finish", min = 1, args = "PLAYERS")
	public void finishAll(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		int success = 0;
		int errors = 0;
		for (Quest q : QuestsAPI.getQuests().getQuestsStarted(acc)) {
			try{
				q.finish(target);
				success++;
			}catch (Throwable ex){
				ex.printStackTrace();
				errors++;
				continue;
			}
		}
		Lang.LEAVE_ALL_RESULT.send(cmd.sender, success, errors);
	}
	
	@Cmd(permission = "finish", min = 2, args = {"PLAYERS", "QUESTSID"})
	public void finish(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		try{
			((Quest) cmd.args[1]).finish(target);
			Lang.LEAVE_ALL_RESULT.send(cmd.sender, 1, 0);
		}catch (Throwable ex){
			ex.printStackTrace();
			Lang.LEAVE_ALL_RESULT.send(cmd.sender, 1, 1);
		}
	}
	
	@Cmd(permission = "setStage", min = 2, args = {"PLAYERS", "QUESTSID", "0|1|2|3|4|5|6|7|8|9|10|11|12|13|14", "0|1|2|3|4|5|6|7|8|9|10|11|12|13|14"})
	public void setStage(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		Quest qu = (Quest) cmd.args[1];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		BranchesManager manager = qu.getBranchesManager();	// syntax: no arg: next or start | 1 arg: start branch | 2 args: set branch stage
		
		PlayerQuestDatas datas = acc.getQuestDatasIfPresent(qu);
		if (cmd.args.length < 3 && (datas == null || !datas.hasStarted())) { // start quest
			qu.start(target);
			Lang.START_QUEST.send(cmd.sender, qu.getName(), acc.debugName());
			return;
		}
		if (datas == null) datas = acc.getQuestDatas(qu); // creates quest datas
		
		QuestBranch currentBranch = manager.getBranch(datas.getBranch());

		if (cmd.args.length < 3) { // next
			if (!datas.isInEndingStages()) {
				currentBranch.finishStage(target, currentBranch.getRegularStage(datas.getStage()));
				Lang.COMMAND_SETSTAGE_NEXT.send(cmd.sender);
			}else Lang.COMMAND_SETSTAGE_NEXT_UNAVAILABLE.send(cmd.sender);
		}else {
			Integer branchID = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
			if (branchID == null) return;
			QuestBranch branch = manager.getBranch(branchID);
			if (branch == null){
				Lang.COMMAND_SETSTAGE_BRANCH_DOESNTEXIST.send(cmd.sender, branchID);
				return;
			}

			Integer stageID = -1;
			if (cmd.args.length > 3){
				stageID = Utils.parseInt(cmd.sender, (String) cmd.args[3]);
				if (stageID == null) return;
				if (stageID < 0) {
					Lang.NUMBER_NEGATIVE.send(cmd.sender);
					return;
				}
				if (currentBranch == null) {
					Lang.ERROR_OCCURED.send(cmd.sender, "player " + acc.debugName() + " has not started quest");
					return;
				}
				if (currentBranch.getRegularStages().size() <= stageID) {
					Lang.COMMAND_SETSTAGE_STAGE_DOESNTEXIST.send(cmd.sender, stageID);
					return;
				}
			}
			Lang.COMMAND_SETSTAGE_SET.send(cmd.sender, branchID);
			if (currentBranch != null) {
				if (datas.isInEndingStages()) {
					for (AbstractStage stage : currentBranch.getEndingStages().keySet()) stage.end(acc);
				}else {
					currentBranch.getRegularStage(datas.getStage()).end(acc);
				}
			}
			if (cmd.args.length == 3){ // start branch
				branch.start(acc);
			}else { // set stage in branch
				datas.setBranch(branchID);
				branch.setStage(acc, stageID);
			}
			QuestsAPI.propagateQuestsHandlers(handler -> handler.questUpdated(acc, target, qu));
		}
	}
	
	@Cmd (permission = "setStage", min = 2, args = { "PLAYERS", "QUESTSID" })
	public void startDialog(CommandContext cmd) {
		Player target = (Player) cmd.args[0];
		Quest qu = (Quest) cmd.args[1];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		PlayerQuestDatas datas = acc.getQuestDatasIfPresent(qu);
		
		DialogRunner runner = null;
		if (datas == null || !qu.hasStarted(acc)) {
			if (qu.hasOption(OptionStartDialog.class)) {
				runner = qu.getOption(OptionStartDialog.class).getDialogRunner();
			}
		}else {
			if (datas.isInEndingStages() || datas.isInQuestEnd()) {
				Lang.COMMAND_STARTDIALOG_IMPOSSIBLE.send(cmd.sender);
				return;
			}else {
				AbstractStage stage = qu.getBranchesManager().getBranch(datas.getBranch()).getRegularStage(datas.getStage());
				if (stage instanceof Dialogable) {
					runner = ((Dialogable) stage).getDialogRunner();
				}
			}
		}
		
		if (runner == null) {
			Lang.COMMAND_STARTDIALOG_NO.send(cmd.sender);
		}else {
			if (runner.isPlayerInDialog(target)) {
				Lang.COMMAND_STARTDIALOG_ALREADY.send(cmd.sender);
			}else {
				runner.handleNext(target);
				Lang.COMMAND_STARTDIALOG_SUCCESS.send(cmd.sender, target.getName(), qu.getID());
			}
		}
	}

	@Cmd(permission = "resetPlayer", min = 1, args = "PLAYERS")
	public void resetPlayer(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		int quests = 0, pools = 0;
		for (PlayerQuestDatas questDatas : new ArrayList<>(acc.getQuestsDatas())) {
			Quest quest = questDatas.getQuest();
			if (quest != null) {
				quest.resetPlayer(acc);
			}else acc.removeQuestDatas(questDatas.getQuestID());
			quests++;
		}
		for (PlayerPoolDatas poolDatas : new ArrayList<>(acc.getPoolDatas())) {
			QuestPool pool = poolDatas.getPool();
			if (pool != null) {
				pool.resetPlayer(acc);
			}else acc.removePoolDatas(poolDatas.getPoolID());
			pools++;
		}
		if (acc.isCurrent()) Lang.DATA_REMOVED.send(acc.getPlayer(), quests, cmd.sender.getName(), pools);
		Lang.DATA_REMOVED_INFO.send(cmd.sender, quests, target.getName(), pools);
	}
	
	@Cmd(permission = "resetPlayer", min = 1, args = {"PLAYERS", "QUESTSID"})
	public void resetPlayerQuest(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		if (cmd.args.length > 1){
			Quest qu = (Quest) cmd.args[1];
			reset(cmd.sender, target, acc, qu);
		}else if (cmd.isPlayer()){
			QuestsListGUI gui = new QuestsListGUI((obj) -> {
				reset(cmd.sender, target, acc, obj);
			}, acc, true, false, true);
			Inventories.create(cmd.player, gui);
		}else Lang.INCORRECT_SYNTAX.sendWP(cmd.sender);
	}
	
	@Cmd (permission = "resetPlayer", min = 2, args = { "PLAYERS", "POOLSID", "BOOLEAN" })
	public void resetPlayerPool(CommandContext cmd) {
		Player target = (Player) cmd.args[0];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		QuestPool pool = cmd.get(1);
		if (Boolean.parseBoolean(cmd.get(2, "false"))) { // only timer
			pool.resetPlayerTimer(acc);
			Lang.POOL_RESET_TIMER.send(cmd.sender, pool.getID(), target.getName());
		}else {
			pool.resetPlayer(acc);
			Lang.POOL_RESET_FULL.send(cmd.sender, pool.getID(), target.getName());
		}
	}
	
	@Cmd(permission = "seePlayer", player = true, min = 1, args = "PLAYERS")
	public void seePlayer(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		new PlayerListGUI(PlayersManager.getPlayerAccount(target), false).create(cmd.player);
	}
	
	@Cmd(permission = "resetQuest", min = 1, args = {"QUESTSID"})
	public void resetQuest(CommandContext cmd) {
		Quest qu = (Quest) cmd.args[0];
		int amount = 0;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (qu.resetPlayer(PlayersManager.getPlayerAccount(p))) amount++;
		}
		amount += PlayersManager.manager.removeQuestDatas(qu);
		Lang.QUEST_PLAYERS_REMOVED.send(cmd.sender, amount);
	}
	
	@Cmd (permission = "start", min = 1, args = { "PLAYERS", "QUESTSID", "BOOLEAN" })
	public void start(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		boolean testRequirements = !(CommandsManager.hasPermission(cmd.sender, "start.other", false) && (cmd.args.length > 2 ? Boolean.parseBoolean(cmd.<String>get(2)) : false));
		if (cmd.isPlayer()){
			if (target == cmd.player){
				if (!CommandsManager.hasPermission(cmd.player, "start", true)) return;
			}else if (!CommandsManager.hasPermission(cmd.player, "start.other", true)) return;
		}
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		if (cmd.args.length < 2 && cmd.isPlayer()){
			QuestsListGUI gui = new QuestsListGUI((obj) -> {
				Quest qu = obj;
				if (testRequirements && !qu.isLauncheable(target, acc, true)) {
					Lang.START_QUEST_NO_REQUIREMENT.send(cmd.sender, qu.getName());
					return;
				}
				qu.start(target);
				Lang.START_QUEST.send(cmd.sender, qu.getName(), acc.abstractAcc.getIdentifier());
			}, acc, false, true, false);
			Inventories.create(cmd.player, gui);
		}else if (cmd.args.length >= 2){
			Quest qu = (Quest) cmd.args[1];
			if (testRequirements && !qu.isLauncheable(target, acc, true)) {
				Lang.START_QUEST_NO_REQUIREMENT.send(cmd.sender, qu.getName());
				return;
			}
			qu.start(target);
			Lang.START_QUEST.send(cmd.sender, qu.getName(), acc.abstractAcc.getIdentifier());
		}else {
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
		}
	}
	
	@Cmd (permission = "cancel", min = 1, args = { "PLAYERS", "QUESTSID" })
	public void cancel(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		if (cmd.isPlayer()){
			if (target != cmd.player && !CommandsManager.hasPermission(cmd.player, "cancel.other", true)) return;
		}
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		if (acc == null) {
			Lang.PLAYER_DATA_NOT_FOUND.send(cmd.sender, target.getName());
			return;
		}
		
		if (cmd.args.length < 2 && cmd.isPlayer()){
			QuestsListGUI gui = new QuestsListGUI((obj) -> {
				cancelQuest(cmd.sender, acc, obj);
			}, acc, true, false, false);
			Inventories.create(cmd.player, gui);
		}else if (cmd.args.length >= 2){
				cancelQuest(cmd.sender, acc, (Quest) cmd.args[1]);
		}else {
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
		}
	}
	
	@Cmd(permission = "setItem", player = true, min = 1, args = "talk|launch|nolaunch")
	public void setItem(CommandContext cmd){
		String name = (String) cmd.args[0];
		if (!"talk".equalsIgnoreCase(name) && !"launch".equalsIgnoreCase(name) && !"nolaunch".equalsIgnoreCase(name)){
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
			return;
		}
		ItemStack item = cmd.player.getInventory().getItemInMainHand();
		if (item.getType() == Material.AIR) {
			BeautyQuests.getInstance().getDataFile().set(name.toLowerCase() + "Item", null);
			Lang.ITEM_REMOVED.send(cmd.sender);
			return;
		}
		BeautyQuests.getInstance().getDataFile().set(name.toLowerCase() + "Item", item.serialize());
		Lang.ITEM_CHANGED.send(cmd.sender);
	}
	
	@Cmd (permission = "setItem", player = true)
	public void setFirework(CommandContext cmd) {
		if ("none".equals(cmd.get(0, null))) {
			BeautyQuests.getInstance().getDataFile().set("firework", "none");
			Lang.FIREWORK_REMOVED.send(cmd.sender);
			Lang.RESTART_SERVER.send(cmd.sender);
		}else {
			ItemMeta meta = cmd.player.getInventory().getItemInMainHand().getItemMeta();
			if (meta instanceof FireworkMeta) {
				BeautyQuests.getInstance().getDataFile().set("firework", meta);
				Lang.FIREWORK_EDITED.send(cmd.sender);
				Lang.RESTART_SERVER.send(cmd.sender);
			}else {
				Lang.FIREWORK_INVALID_HAND.send(cmd.sender);
			}
		}
	}
	
	@Cmd (permission = "manage", min = 1, args = "save|force")
	public void backup(CommandContext cmd){
		if (cmd.args[0].equals("save")){
			save(cmd);
		}else if (!cmd.args[0].equals("force")){
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
			return;
		}
		
		boolean success = true;
		BeautyQuests.logger.info("Creating backup due to " + cmd.sender.getName() + "'s manual command.");
		if (!BeautyQuests.getInstance().createFolderBackup()) {
			Lang.BACKUP_QUESTS_FAILED.send(cmd.sender);
			success = false;
		}
		if (!BeautyQuests.getInstance().createDataBackup()) {
			Lang.BACKUP_PLAYERS_FAILED.send(cmd.sender);
			success = false;
		}
		if (success) Lang.BACKUP_CREATED.send(cmd.sender);
	}
	
	@Cmd(permission = "adminMode")
	public void adminMode(CommandContext cmd){
		AdminMode.toggle(cmd.sender);
	}
	
	@Cmd (player = true, hide = true)
	public void exitEditor(CommandContext cmd){
		Editor.leave(cmd.player);
		Inventories.closeAndExit(cmd.player);
	}
	
	@Cmd (player = true, hide = true)
	public void reopenInventory(CommandContext cmd){
		if (Inventories.isInSystem(cmd.player)){
			Inventories.openInventory(cmd.player);
		}
	}
	
	@Cmd(permission = "list", player = true)
	public void list(CommandContext cmd){
		if (NMS.isValid()){
			ListBook.openQuestBook(cmd.player);
		}else Utils.sendMessage(cmd.sender, "Version not supported");
	}
	
	@Cmd (args = { "PLAYERS", "setline|removeline|resetline|resetall|hide|show" })
	public void scoreboard(CommandContext cmd){
		if (cmd.args.length == 0) {
			if (!cmd.isPlayer()) {
				Lang.MUST_PLAYER.sendWP(cmd.sender);
				return;
			}
			
			if (!CommandsManager.hasPermission(cmd.sender, "scoreboard.toggle", true)) return;
			Scoreboard board = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(cmd.player);
			if (board.isForceHidden()) {
				board.show(true);
				Lang.COMMAND_SCOREBOARD_OWN_SHOWN.send(cmd.player);
			}else {
				board.hide(true);
				Lang.COMMAND_SCOREBOARD_OWN_HIDDEN.send(cmd.player);
			}
		}else {
			if (!CommandsManager.hasPermission(cmd.sender, "scoreboard", true)) return;
			if (cmd.args.length < 2) {
				Lang.INCORRECT_SYNTAX.sendWP(cmd.sender);
				return;
			}
			Player p = (Player) cmd.args[0];
			Scoreboard board = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(p);
			
			switch (((String) cmd.args[1]).toLowerCase()) {
			case "setline":
				if (cmd.args.length < 4) {
					Lang.INCORRECT_SYNTAX.send(cmd.sender);
					break;
				}
				Integer id = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
				if (id == null) return;
				board.setCustomLine(id, Utils.buildFromArray(cmd.args, 3, " "));
				Lang.COMMAND_SCOREBOARD_LINESET.send(cmd.sender, id);
				break;
			case "removeline":
				if (cmd.args.length < 3) {
					Lang.INCORRECT_SYNTAX.send(cmd.sender);
					break;
				}
				id = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
				if (id == null) return;
				if (board.removeLine(id)) {
					Lang.COMMAND_SCOREBOARD_LINEREMOVE.send(cmd.sender, id);
				}else Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(cmd.sender, id);
				break;
			case "resetline":
				if (cmd.args.length < 3) {
					Lang.INCORRECT_SYNTAX.send(cmd.sender);
					break;
				}
				id = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
				if (id == null) return;
				if (board.resetLine(id)) {
					Lang.COMMAND_SCOREBOARD_LINERESET.send(cmd.sender, id);
				}else Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(cmd.sender, id);
				break;
			case "resetall":
				BeautyQuests.getInstance().getScoreboardManager().removePlayerScoreboard(p);
				BeautyQuests.getInstance().getScoreboardManager().create(p);
				Lang.COMMAND_SCOREBOARD_RESETALL.send(cmd.sender, p.getName());
				break;
			case "hide":
				board.hide(true);
				Lang.COMMAND_SCOREBOARD_HIDDEN.send(cmd.sender, p.getName());
				break;
			case "show":
				board.show(true);
				Lang.COMMAND_SCOREBOARD_SHOWN.send(cmd.sender, p.getName());
				break;
			default:
				Lang.INCORRECT_SYNTAX.send(cmd.sender);
				break;
			}
		}
	}
	
	@Cmd (player = true, permission = "pools")
	public void pools(CommandContext cmd) {
		PoolsManageGUI.get().create(cmd.player);
	}
	
	@Cmd (player = true, args = "QUESTSID", min = 1)
	public void checkpoint(CommandContext cmd) {
		Quest quest = cmd.get(0);
		PlayerAccount account = PlayersManager.getPlayerAccount(cmd.player);
		if (account.hasQuestDatas(quest)) {
			PlayerQuestDatas datas = account.getQuestDatas(quest);
			QuestBranch branch = quest.getBranchesManager().getBranch(datas.getBranch());
			int max = datas.isInEndingStages() ? branch.getStageSize() : datas.getStage();
			for (int id = max - 1; id >= 0; id--) {
				AbstractStage stage = branch.getRegularStage(id);
				Optional<CheckpointReward> optionalCheckpoint = stage.getRewards().stream().filter(CheckpointReward.class::isInstance).findAny().map(CheckpointReward.class::cast);
				if (optionalCheckpoint.isPresent()) {
					optionalCheckpoint.get().applies(cmd.player);
					return;
				}
			}
			Lang.COMMAND_CHECKPOINT_NO.send(cmd.sender, quest.getName());
		}else Lang.COMMAND_CHECKPOINT_NOT_STARTED.send(cmd.sender);
	}
	
	@Cmd (permission = "manage")
	public void downloadTranslations(CommandContext cmd) {
		if (NMS.getMCVersion() < 13) {
			Utils.sendMessage(cmd.sender, "§c" + Lang.VERSION_REQUIRED.toString(), "≥ 1.13");
			return;
		}
		if (cmd.args.length == 0) {
			Lang.COMMAND_TRANSLATION_SYNTAX.send(cmd.sender);
			return;
		}
		String lang = cmd.<String>get(0).toLowerCase();
		String version = NMS.getVersionString();
		String url = MinecraftNames.LANG_DOWNLOAD_URL.replace("%version%", version).replace("%language%", lang);
		
		try {
			File destination = new File(BeautyQuests.getInstance().getDataFolder(), lang + ".json");
			if (destination.isDirectory()) {
				Lang.ERROR_OCCURED.send(cmd.sender, lang + ".json is a directory");
				return;
			}
			if (!(cmd.args.length > 1 && Boolean.parseBoolean(cmd.get(1))) && destination.exists()) {
				Lang.COMMAND_TRANSLATION_EXISTS.send(cmd.sender, lang + ".json");
				return;
			}
			try (ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream())) {
				destination.createNewFile();
				try (FileOutputStream output = new FileOutputStream(destination)) {
					output.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
					Lang.COMMAND_TRANSLATION_DOWNLOADED.send(cmd.sender, lang);
				}
			}catch (FileNotFoundException ex) {
				Lang.COMMAND_TRANSLATION_NOT_FOUND.send(cmd.sender, lang, version);
			}
		}catch (IOException e) {
			Lang.ERROR_OCCURED.send(cmd.sender, "IO Exception when downloading translation.");
			BeautyQuests.logger.severe("An error occurred while downloading translation.", e);
		}
	}
	
	@Cmd (permission = "manage")
	public void migrateDatas(CommandContext cmd) {
		if (!(PlayersManager.manager instanceof PlayersManagerYAML)) {
			cmd.sender.sendMessage("§cYou can't migrate YAML datas to a DB system if you are already using the DB system.");
			return;
		}
		Utils.runAsync(() -> {
			cmd.sender.sendMessage("§aConnecting to the database.");
			Database db = null;
			try {
				db = new Database(BeautyQuests.getInstance().getConfig().getConfigurationSection("database"));
				db.testConnection();
				cmd.sender.sendMessage("§aConnection to database etablished.");
				final Database fdb = db;
				Utils.runSync(() -> {
					cmd.sender.sendMessage("§aStarting migration...");
					try {
						cmd.sender.sendMessage(PlayersManagerDB.migrate(fdb, (PlayersManagerYAML) PlayersManager.manager));
					}catch (Exception ex) {
						cmd.sender.sendMessage("§cAn exception occured during migration. Process aborted. " + ex.getMessage());
						ex.printStackTrace();
					}
				});
			}catch (SQLException ex) {
				cmd.sender.sendMessage("§cConnection to database has failed. Aborting. " + ex.getMessage());
				BeautyQuests.logger.severe("An error occurred while connecting to the database for datas migration.", ex);
				if (db != null) db.closeConnection();
			}
		});
	}

	@Cmd(permission = "help")
	public void help(CommandContext cmd){
		for (Lang l : Lang.values()){
			if (l.getPath().startsWith("msg.command.help.")){
				String command = l.getPath().substring(17);
				if (command.equals("header")){
					l.sendWP(cmd.sender);
				}else if (CommandsManager.hasPermission(cmd.sender, cmd.manager.commands.get(command.toLowerCase()).cmd.permission(), false)) l.sendWP(cmd.sender, cmd.label);
			}
		}
	}
	
	private static void reset(CommandSender sender, Player target, PlayerAccount acc, Quest qu){
		qu.resetPlayer(acc);
		if (acc.isCurrent()) Lang.DATA_QUEST_REMOVED.send(target, qu.getName(), sender.getName());
		Lang.DATA_QUEST_REMOVED_INFO.send(sender, target.getName(), qu.getName());
	}
	
	private static void remove(CommandSender sender, Quest quest){
		if (sender instanceof Player){
			Inventories.create((Player) sender, new ConfirmGUI(() -> {
				quest.remove(true, true);
				Lang.SUCCESFULLY_REMOVED.send(sender, quest.getName());
			}, ((Player) sender)::closeInventory, Lang.INDICATION_REMOVE.format(quest.getName())));
		}else {
			quest.remove(true, true);
			Lang.SUCCESFULLY_REMOVED.send(sender, quest.getName());
		}
	}

	private static void cancelQuest(CommandSender sender, PlayerAccount acc, Quest qu){
		if (!sender.hasPermission("beautyquests.command.cancel.other") && !qu.isCancellable()){
			Lang.CANCEL_QUEST_UNAVAILABLE.send(sender, qu.getName());
			return;
		}
		qu.cancelPlayer(acc);
		Lang.CANCEL_QUEST.send(sender, qu.getName());
	}
	
}
