package fr.skytasul.quests.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.Quest;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.editors.Editor;
import fr.skytasul.quests.editors.SelectNPC;
import fr.skytasul.quests.gui.Inventories;
import fr.skytasul.quests.gui.creation.stages.StagesGUI;
import fr.skytasul.quests.gui.misc.ChooseAccountGUI;
import fr.skytasul.quests.gui.misc.ConfirmGUI;
import fr.skytasul.quests.gui.misc.ListBook;
import fr.skytasul.quests.gui.quests.ChooseQuestGUI;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.gui.quests.QuestsListGUI;
import fr.skytasul.quests.players.AdminMode;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.scoreboards.Scoreboard;
import fr.skytasul.quests.stages.StageManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.nms.NMS;
import net.citizensnpcs.api.npc.NPC;

public class Commands {
	
	@Cmd(permission = "create", player = true, noEditorInventory = true)
	public void create(CommandContext cmd){
		Inventories.create(cmd.player, new StagesGUI());
	}
	
	@Cmd(permission = "edit", player = true, noEditorInventory = true)
	public void edit(CommandContext cmd){
		Lang.CHOOSE_NPC_STARTER.send(cmd.player);
		new SelectNPC(cmd.player, (obj) -> {
			if (obj == null) return;
			NPC npc = (NPC) obj;
			if (QuestsAPI.isQuestStarter(npc)){
				Inventories.create(cmd.player, new ChooseQuestGUI(QuestsAPI.getQuestsAssigneds(npc), (quObj) -> {
						if (quObj == null) return;
						Inventories.create(cmd.player, new StagesGUI()).edit((Quest) quObj);
				}));
			}else {
				Lang.NPC_NOT_QUEST.send(cmd.player);
			}
		}).enterOrLeave(cmd.player);
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
		new SelectNPC(cmd.player, (obj) -> {
			if (obj == null) return;
			NPC npc = (NPC) obj;
			if (QuestsAPI.isQuestStarter(npc)){
				Inventories.create(cmd.player, new ChooseQuestGUI(QuestsAPI.getQuestsAssigneds(npc), (quObj) -> {
						remove(cmd.sender, (Quest) quObj);
				}));
			}else {
				Lang.NPC_NOT_QUEST.send(cmd.sender);
			}
		}).enterOrLeave(cmd.player);
	}

	@Cmd(permission = "reload")
	public void reload(CommandContext cmd){
		BeautyQuests.getInstance().performReload(cmd.sender);
	}
	
	@Cmd(permission = "version")
	public void version(CommandContext cmd){
		cmd.sender.sendMessage("§eBeautyQuests version : §6§l" + BeautyQuests.getInstance().getDescription().getVersion());
	}
	
	@Cmd(permission = "reload")
	public void save(CommandContext cmd){
		try {
			int amount = BeautyQuests.getInstance().saveAllConfig(false);
			cmd.sender.sendMessage("§a" + amount + " quests saved");
			BeautyQuests.logger.info(amount + " quests saved ~ manual save from " + cmd.sender.getName());
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
		for (Quest q : QuestsAPI.getQuestsStarteds(acc)){
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
	
	@Cmd(permission = "setStage", min = 2, args = {"PLAYERS", "QUESTSID", "0|1|2|3|4|5|6|7|8|9|10|11|12|13|14"})
	public void setStage(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		Quest qu = (Quest) cmd.args[1];
		StageManager manager = qu.getStageManager();
		if (cmd.args.length != 3) {
			manager.next(target);
		}else {
			Integer st = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
			if (st == null) return;
			PlayerAccount acc = PlayersManager.getPlayerAccount(target);
			if (qu.hasStarted(acc)) {
				AbstractStage active = manager.getPlayerStage(acc);
				BukkitRunnable run = new BukkitRunnable() {
					public void run(){
						manager.finishStage(acc, active);
						manager.setStage(acc, st, true);
					}
				};
				if (active.hasAsyncEnd()) {
					run.runTaskAsynchronously(BeautyQuests.getInstance());
				}else run.run();
			}else manager.setStage(acc, st, true);
		}
	}
	
	@Cmd(permission = "resetPlayer", min = 1, args = "PLAYERS")
	public void resetPlayer(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		int i = 0;
		for (Quest q : BeautyQuests.getInstance().getQuests()){
			if (q.resetPlayer(acc)) i++;
		}
		if (acc.isCurrent()) Lang.DATA_REMOVED.send(acc.getPlayer(), i, cmd.sender.getName());
		Lang.DATA_REMOVED_INFO.send(cmd.sender, i, target.getName());
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
				reset(cmd.sender, target, acc, (Quest) obj);
			}, acc, true, false, true);
			Inventories.create(cmd.player, gui);
		}else Lang.INCORRECT_SYNTAX.sendWP(cmd.sender);
	}
	
	@Cmd(permission = "seePlayer", player = true, min = 1, args = "PLAYERS")
	public void seePlayer(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		Inventories.create(cmd.player, new ChooseAccountGUI(target.getUniqueId(), (obj) -> {
			Inventories.create(cmd.player, new PlayerListGUI((PlayerAccount) obj));
		}));
	}
	
	@Cmd(min = 1, args = {"PLAYERS", "QUESTSID"})
	public void start(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		if (cmd.isPlayer()){
			if (target == cmd.player){
				if (!CommandsManager.hasPermission(cmd.player, "start")){
					Lang.PERMISSION_REQUIRED.sendWP(cmd.sender, "beautyquests.command.start");
					return;
				}
			}else if (!CommandsManager.hasPermission(cmd.player, "start.other")){
				Lang.PERMISSION_REQUIRED.sendWP(cmd.sender, "beautyquests.command.start.other");
				return;
			}
		}
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		if (cmd.args.length < 2 && cmd.isPlayer()){
			QuestsListGUI gui = new QuestsListGUI((obj) -> {
				Quest qu = (Quest) obj;
				if (!CommandsManager.hasPermission(cmd.player, "start.other") && !qu.isLauncheable(target, true)) return;
				qu.start(target);
				Lang.START_QUEST.send(cmd.sender, qu.getName(), acc.abstractAcc.getIdentifier());
			}, acc, false, true, false);
			Inventories.create(cmd.player, gui);
		}else if (cmd.args.length >= 2){
				Quest qu = (Quest) cmd.args[1];
				qu.start(target);
				Lang.START_QUEST.send(cmd.sender, qu.getName(), acc.abstractAcc.getIdentifier());
		}else {
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
		}
	}
	
	@Cmd(min = 1, args = {"PLAYERS", "QUESTSID"})
	public void cancel(CommandContext cmd){
		Player target = (Player) cmd.args[0];
		if (cmd.isPlayer()){
			if (target == cmd.player){
				if (!CommandsManager.hasPermission(cmd.player, "cancel")){
					Lang.PERMISSION_REQUIRED.sendWP(cmd.sender, "beautyquests.command.cancel");
					return;
				}
			}else if (!CommandsManager.hasPermission(cmd.player, "cancel.other")){
				Lang.PERMISSION_REQUIRED.sendWP(cmd.sender, "beautyquests.command.cancel.other");
				return;
			}
		}
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		if (cmd.args.length < 2 && cmd.isPlayer()){
			QuestsListGUI gui = new QuestsListGUI((obj) -> {
				Quest qu = (Quest) obj;
				qu.cancelPlayer(acc);
				Lang.CANCEL_QUEST.send(cmd.sender, qu.getName());
			}, acc, true, false, false);
			Inventories.create(cmd.player, gui);
		}else if (cmd.args.length >= 2){
				Quest qu = (Quest) cmd.args[1];
				qu.cancelPlayer(acc);
				Lang.CANCEL_QUEST.send(cmd.sender, qu.getName());
		}else {
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
		}
	}
	
	@Cmd(permission = "setItem", player = true, min = 1, args = "talk|launch")
	public void setItem(CommandContext cmd){
		if (!"talk".equalsIgnoreCase((String) cmd.args[0]) && !"launch".equalsIgnoreCase((String) cmd.args[0])){
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
			return;
		}
		ItemStack item = cmd.player.getInventory().getItemInMainHand();
		if (item == null || item.getType() == Material.AIR){
			Lang.MUST_HOLD_ITEM.send(cmd.sender);
			return;
		}
		BeautyQuests.getInstance().getDataFile().set(((String) cmd.args[0]).toLowerCase() + "Item", item.serialize());
		Lang.ITEM_CHANGED.send(cmd.sender);
	}
	
	@Cmd(permission = "reload", min = 1, args = "save|force")
	public void backup(CommandContext cmd){
		if (cmd.args[0].equals("save")){
			save(cmd);
		}else if (!cmd.args[0].equals("force")){
			Lang.INCORRECT_SYNTAX.send(cmd.sender);
			return;
		}
		
		boolean success = true;
		if (!BeautyQuests.getInstance().createFolderBackup(cmd.sender.getName() + "'s manual command.")){
			Lang.BACKUP_QUESTS_FAILED.send(cmd.sender);
			success = false;
		}
		if (!BeautyQuests.getInstance().createDataBackup(cmd.sender.getName() + "'s manual command.")){
			Lang.BACKUP_PLAYERS_FAILED.send(cmd.sender);
			success = false;
		}
		if (success) Lang.BACKUP_CREATED.send(cmd.sender);
	}
	
	@Cmd(permission = "adminMode", player = true)
	public void adminMode(CommandContext cmd){
		AdminMode.toggle(cmd.player);
	}
	
	@Cmd(player = true)
	public void editor(CommandContext cmd){
		Editor.leave(cmd.player);
	}
	
	@Cmd(player = true)
	public void inventory(CommandContext cmd){
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
	
	@Cmd(permission = "scoreboard", min = 2, args = {"PLAYERS", "setline|removeline|resetline|resetall"})
	public void scoreboard(CommandContext cmd){
		Player p = (Player) cmd.args[0];
		Scoreboard board = BeautyQuests.getInstance().getScoreboardManager().getPlayerScoreboard(p);
		switch (((String) cmd.args[1]).toLowerCase()){
		case "setline":
			if (cmd.args.length < 4){
				Lang.INCORRECT_SYNTAX.send(cmd.sender);
				break;
			}
			Integer id = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
			if (id == null) return;
			board.setCustomLine(id, Utils.buildFromArray(cmd.args, 3, " "));
			Lang.COMMAND_SCOREBOARD_LINESET.send(cmd.sender, id);
			break;
		case "removeline":
			if (cmd.args.length < 3){
				Lang.INCORRECT_SYNTAX.send(cmd.sender);
				break;
			}
			id = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
			if (id == null) return;
			if (board.removeLine(id)){
				Lang.COMMAND_SCOREBOARD_LINEREMOVE.send(cmd.sender, id);
			}else Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(cmd.sender, id);
			break;
		case "resetline":
			if (cmd.args.length < 3){
				Lang.INCORRECT_SYNTAX.send(cmd.sender);
				break;
			}
			id = Utils.parseInt(cmd.sender, (String) cmd.args[2]);
			if (id == null) return;
			if (board.resetLine(id)){
				Lang.COMMAND_SCOREBOARD_LINERESET.send(cmd.sender, id);
			}else Lang.COMMAND_SCOREBOARD_LINENOEXIST.send(cmd.sender, id);
			break;
		case "resetall":
			BeautyQuests.getInstance().getScoreboardManager().removePlayerScoreboard(p);
			BeautyQuests.getInstance().getScoreboardManager().create(p);
			Lang.COMMAND_SCOREBOARD_RESETALL.send(cmd.sender, p.getName());
			break;
		}
	}
	
	@Cmd(permission = "help")
	public void help(CommandContext cmd){
		for (Lang l : Lang.values()){
			if (l.getPath().startsWith("msg.command.help.")){
				String command = l.getPath().substring(17);
				if (command.equals("header")){
					l.sendWP(cmd.sender);
				}else if (CommandsManager.hasPermission(cmd.sender, cmd.manager.commands.get(command.toLowerCase()).cmd.permission())) l.sendWP(cmd.sender, cmd.label);
			}
		}
	}
	
	@Cmd(permission = "reload")
	public void removeDuplicate(CommandContext cmd){
		PlayersManager.debugDuplicate(cmd.sender);
	}
	
	
	private static void reset(CommandSender sender, Player target, PlayerAccount acc, Quest qu){
		qu.resetPlayer(acc);
		if (acc.isCurrent()) Lang.DATA_QUEST_REMOVED.send(target, qu.getName(), sender.getName());
		Lang.DATA_QUEST_REMOVED_INFO.send(sender, target.getName(), qu.getName());
	}
	
	private static void remove(CommandSender sender, Quest quest){
		if (sender instanceof Player){
			Inventories.create((Player) sender, new ConfirmGUI(() -> {
				quest.remove(true);
				Lang.SUCCESFULLY_REMOVED.send(sender, quest.getName());
			}, () -> {
				((Player) sender).closeInventory();
			}));
		}else {
			quest.remove(true);
			Lang.SUCCESFULLY_REMOVED.send(sender, quest.getName());
		}
	}
	
}
