package fr.skytasul.quests.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.events.accounts.PlayerAccountResetEvent;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.types.Dialogable;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.gui.quests.QuestsListGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayerPoolDatas;
import fr.skytasul.quests.players.PlayerQuestDatas;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.BranchesManager;
import fr.skytasul.quests.structure.Quest;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.types.DialogRunner;
import fr.skytasul.quests.utils.types.DialogRunner.DialogNextReason;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Switch;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandPermission;
import revxrsal.commands.bukkit.EntitySelector;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.orphan.OrphanCommand;

public class CommandsPlayerManagement implements OrphanCommand {
	
	private BukkitCommandPermission startOtherPermission = new BukkitCommandPermission(new Permission("beautyquests.command.start.other"));
	private BukkitCommandPermission cancelOtherPermission = new BukkitCommandPermission(new Permission("beautyquests.command.cancel.other"));
	
	@Subcommand ("finishAll")
	@CommandPermission ("beautyquests.command.finish")
	public void finishAll(BukkitCommandActor actor, EntitySelector<Player> players) {
		for (Player player : players) {
			PlayerAccount acc = PlayersManager.getPlayerAccount(player);
			int success = 0;
			int errors = 0;
			for (Quest q : QuestsAPI.getQuests().getQuestsStarted(acc)) {
				try {
					q.finish(player);
					success++;
				}catch (Exception ex) {
					BeautyQuests.logger.severe("An error occurred while finishing quest " + q.getID(), ex);
					errors++;
				}
			}
			Lang.LEAVE_ALL_RESULT.send(actor.getSender(), success, errors);
		}
	}
	
	@Subcommand ("finish")
	@CommandPermission ("beautyquests.command.finish")
	public void finish(BukkitCommandActor actor, EntitySelector<Player> players, Quest quest, @Switch boolean force) {
		for (Player player : players) {
			try {
				if (force || quest.hasStarted(PlayersManager.getPlayerAccount(player))) {
					quest.finish(player);
					Lang.LEAVE_ALL_RESULT.send(actor.getSender(), 1, 0);
				}
			}catch (Exception ex) {
				BeautyQuests.logger.severe("An error occurred while finishing quest " + quest.getID(), ex);
				Lang.LEAVE_ALL_RESULT.send(actor.getSender(), 1, 1);
			}
		}
	}
	
	@Subcommand ("setStage")
	@CommandPermission ("beautyquests.command.setStage")
	public void setStage(
			BukkitCommandActor actor,
			Player player,
			Quest quest,
			@Range (min = 0, max = 14) @Optional Integer branchID,
			@Range (min = 0, max = 14) @Optional Integer stageID) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		BranchesManager manager = quest.getBranchesManager(); // syntax: no arg: next or start | 1 arg: start branch | 2 args: set branch stage
		
		PlayerQuestDatas datas = acc.getQuestDatasIfPresent(quest);
		if (branchID == null && (datas == null || !datas.hasStarted())) { // start quest
			quest.start(player);
			Lang.START_QUEST.send(actor.getSender(), quest.getName(), acc.debugName());
			return;
		}
		if (datas == null) datas = acc.getQuestDatas(quest); // creates quest datas
		
		QuestBranch currentBranch = manager.getBranch(datas.getBranch());
		
		if (branchID == null) { // next
			if (!datas.isInEndingStages()) {
				currentBranch.finishStage(player, currentBranch.getRegularStage(datas.getStage()));
				Lang.COMMAND_SETSTAGE_NEXT.send(actor.getSender());
			}else Lang.COMMAND_SETSTAGE_NEXT_UNAVAILABLE.send(actor.getSender());
		}else {
			QuestBranch branch = manager.getBranch(branchID);
			if (branch == null)
				throw new CommandErrorException(Lang.COMMAND_SETSTAGE_BRANCH_DOESNTEXIST.format(branchID));
			
			if (stageID != null) {
				if (currentBranch == null)
					throw new CommandErrorException(Lang.ERROR_OCCURED.format("player " + acc.debugName() + " has not started quest"));
				if (branch.getRegularStages().size() <= stageID)
					throw new CommandErrorException(Lang.COMMAND_SETSTAGE_STAGE_DOESNTEXIST.format(stageID));
			}
			Lang.COMMAND_SETSTAGE_SET.send(actor.getSender(), stageID);
			if (currentBranch != null) {
				if (datas.isInEndingStages()) {
					for (AbstractStage stage : currentBranch.getEndingStages().keySet()) stage.end(acc);
				}else {
					currentBranch.getRegularStage(datas.getStage()).end(acc);
				}
			}
			if (stageID == null) { // start branch
				branch.start(acc);
			}else { // set stage in branch
				datas.setBranch(branchID);
				branch.setStage(acc, stageID);
			}
			QuestsAPI.propagateQuestsHandlers(handler -> handler.questUpdated(acc, player, quest));
		}
	}
	
	@Subcommand ("startDialog")
	@CommandPermission ("beautyquests.command.setStage")
	public void startDialog(BukkitCommandActor actor, Player player, Quest quest) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		PlayerQuestDatas datas = acc.getQuestDatasIfPresent(quest);
		
		DialogRunner runner = null;
		if (datas == null || !quest.hasStarted(acc)) {
			if (quest.hasOption(OptionStartDialog.class)) {
				runner = quest.getOption(OptionStartDialog.class).getDialogRunner();
			}
		}else {
			if (datas.isInEndingStages() || datas.isInQuestEnd()) {
				Lang.COMMAND_STARTDIALOG_IMPOSSIBLE.send(actor.getSender());
				return;
			}else {
				AbstractStage stage = quest.getBranchesManager().getBranch(datas.getBranch()).getRegularStage(datas.getStage());
				if (stage instanceof Dialogable) {
					runner = ((Dialogable) stage).getDialogRunner();
				}
			}
		}
		
		if (runner == null) {
			Lang.COMMAND_STARTDIALOG_NO.send(actor.getSender());
		}else {
			if (runner.isPlayerInDialog(player)) {
				Lang.COMMAND_STARTDIALOG_ALREADY.send(actor.getSender());
			}else {
				runner.handleNext(player, DialogNextReason.COMMAND);
				Lang.COMMAND_STARTDIALOG_SUCCESS.send(actor.getSender(), player.getName(), quest.getID());
			}
		}
	}
	
	@Subcommand ("resetPlayer")
	@CommandPermission ("beautyquests.command.resetPlayer")
	public void resetPlayer(BukkitCommandActor actor, EntitySelector<Player> players) {
		for (Player player : players) {
			PlayerAccount acc = PlayersManager.getPlayerAccount(player);

			List<CompletableFuture<?>> futures = new ArrayList<>(acc.getQuestsDatas().size() + acc.getPoolDatas().size());

			int quests = 0, pools = 0;
			for (PlayerQuestDatas questDatas : new ArrayList<>(acc.getQuestsDatas())) {
				Quest quest = questDatas.getQuest();
				CompletableFuture<?> future =
						quest == null ? acc.removeQuestDatas(questDatas.getQuestID()) : quest.resetPlayer(acc);
				future = future.whenComplete(BeautyQuests.logger.logError("An error occurred while resetting quest "
						+ questDatas.getQuestID() + " to player " + player.getName(), actor.getSender()));
				futures.add(future);
				quests++;
			}
			for (PlayerPoolDatas poolDatas : new ArrayList<>(acc.getPoolDatas())) {
				QuestPool pool = poolDatas.getPool();
				CompletableFuture<?> future =
						pool == null ? acc.removePoolDatas(poolDatas.getPoolID()) : pool.resetPlayer(acc);
				future = future.whenComplete(BeautyQuests.logger.logError(
						"An error occurred while resetting pool " + poolDatas.getPoolID() + " to player " + player.getName(),
						actor.getSender()));
				futures.add(future);
				pools++;
			}
			acc.resetDatas();

			final int questsFinal = quests;
			final int poolsFinal = pools;
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete(Utils.runSyncConsumer(() -> {
				Bukkit.getPluginManager().callEvent(new PlayerAccountResetEvent(player, acc));
				if (acc.isCurrent())
					Lang.DATA_REMOVED.send(player, questsFinal, actor.getName(), poolsFinal);
				Lang.DATA_REMOVED_INFO.send(actor.getSender(), questsFinal, player.getName(), poolsFinal);
			}));

		}
	}
	
	@Subcommand ("resetPlayerQuest")
	@CommandPermission ("beautyquests.command.resetPlayer")
	public void resetPlayerQuest(BukkitCommandActor actor, Player player, @Optional Quest quest) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		if (quest != null) {
			reset(actor.getSender(), player, acc, quest);
		}else {
			new QuestsListGUI(obj -> {
				reset(actor.getSender(), player, acc, obj);
			}, acc, true, false, true).create(actor.requirePlayer());
		}
	}
	
	private void reset(CommandSender sender, Player target, PlayerAccount acc, Quest qu) {
		qu.resetPlayer(acc).whenComplete(BeautyQuests.logger.logError(__ -> {
			if (acc.isCurrent())
				Lang.DATA_QUEST_REMOVED.send(target, qu.getName(), sender.getName());
			Lang.DATA_QUEST_REMOVED_INFO.send(sender, target.getName(), qu.getName());
		}, "An error occurred while removing player quest data", sender));
	}
	
	@Subcommand ("resetQuest")
	@CommandPermission ("beautyquests.command.resetQuest")
	public void resetQuest(BukkitCommandActor actor, Quest quest) {
		List<CompletableFuture<Boolean>> futures = new ArrayList<>(Bukkit.getOnlinePlayers().size());

		for (Player p : Bukkit.getOnlinePlayers()) {
			futures.add(quest.resetPlayer(PlayersManager.getPlayerAccount(p))
					.whenComplete(BeautyQuests.logger.logError(
							"An error occurred while resetting quest " + quest.getID() + " to player " + p.getName(),
							actor.getSender())));
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((__, ___) -> {
			// we do not care about failure or success of this "global" future

			int resetAmount =
					(int) futures.stream().filter(future -> {
						try {
							return !future.isCompletedExceptionally() && future.get();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						} catch (ExecutionException ignored) {
							// we already check if the future is completed exceptionnally before using get()
						}
						return false;
					}).count();

			BeautyQuests.getInstance().getPlayersManager().removeQuestDatas(quest)
					.whenComplete(BeautyQuests.logger.logError(removedAmount -> {
						Lang.QUEST_PLAYERS_REMOVED.send(actor.getSender(), removedAmount + resetAmount);
					}, "An error occurred while removing quest datas", actor.getSender()));
		}).whenComplete(BeautyQuests.logger.logError());

	}
	
	@Subcommand ("seePlayer")
	@CommandPermission ("beautyquests.command.seePlayer")
	public void seePlayer(Player actor, Player player) {
		new PlayerListGUI(PlayersManager.getPlayerAccount(player), false).create(actor);
	}
	
	@Subcommand ("start")
	@CommandPermission ("beautyquests.command.start")
	public void start(BukkitCommandActor actor, ExecutableCommand command, EntitySelector<Player> players, @Optional Quest quest, @CommandPermission ("beautyquests.command.start.other") @Switch boolean overrideRequirements) {
		if (actor.isPlayer() && !startOtherPermission.canExecute(actor)) {
			if (players.isEmpty() || players.size() > 1 || (players.get(0) != actor.getAsPlayer()))
				throw new NoPermissionException(command, startOtherPermission);
		}
		
		for (Player player : players) {
			PlayerAccount acc = PlayersManager.getPlayerAccount(player);
			
			if (quest == null) {
				new QuestsListGUI(obj -> {
					start(actor.getSender(), player, acc, obj, overrideRequirements);
				}, acc, false, true, false).create(actor.requirePlayer());
			}else {
				start(actor.getSender(), player, acc, quest, overrideRequirements);
			}
		}
	}

	private void start(CommandSender sender, Player player, PlayerAccount acc, Quest quest, boolean overrideRequirements) {
		if (!overrideRequirements && !(quest.isLauncheable(player, acc, true) && quest.testTimer(acc, true))) {
			Lang.START_QUEST_NO_REQUIREMENT.send(sender, quest.getName());
			return;
		}
		quest.start(player);
		Lang.START_QUEST.send(sender, quest.getName(), acc.abstractAcc.getIdentifier());
	}
	
	@Subcommand ("cancel")
	@CommandPermission ("beautyquests.command.cancel")
	public void cancel(BukkitCommandActor actor, ExecutableCommand command, EntitySelector<Player> players, @Optional Quest quest) {
		if (actor.isPlayer() && !cancelOtherPermission.canExecute(actor)) {
			if (players.isEmpty() || players.size() > 1 || (players.get(0) != actor.getAsPlayer()))
				throw new NoPermissionException(command, cancelOtherPermission);
		}
		
		for (Player player : players) {
			PlayerAccount acc = PlayersManager.getPlayerAccount(player);
			
			if (quest == null) {
				new QuestsListGUI(obj -> {
					cancel(actor.getSender(), acc, obj);
				}, acc, true, false, false).create(actor.requirePlayer());
			}else {
				cancel(actor.getSender(), acc, quest);
			}
		}
	}
	
	private void cancel(CommandSender sender, PlayerAccount acc, Quest quest) {
		if (!quest.isCancellable()) {
			Lang.CANCEL_QUEST_UNAVAILABLE.send(sender, quest.getName());
			return;
		}

		if (quest.cancelPlayer(acc)) {
			Lang.CANCEL_QUEST.send(sender, quest.getName());
		} else {
			if (sender.equals(acc.getPlayer())) {
				Lang.QUEST_NOT_STARTED.send(sender);
			} else {
				Lang.ERROR_OCCURED.send(sender,
						"Player " + acc.getName() + " does not have the quest " + quest.getID() + " started.");
			}
		}
	}
	
}
