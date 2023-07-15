package fr.skytasul.quests.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.accounts.PlayerAccountResetEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner.DialogNextReason;
import fr.skytasul.quests.api.players.PlayerAccount;
import fr.skytasul.quests.api.players.PlayerPoolDatas;
import fr.skytasul.quests.api.players.PlayerQuestDatas;
import fr.skytasul.quests.api.players.PlayersManager;
import fr.skytasul.quests.api.pools.QuestPool;
import fr.skytasul.quests.api.quests.Quest;
import fr.skytasul.quests.api.quests.branches.EndingStage;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.types.Dialogable;
import fr.skytasul.quests.api.utils.messaging.DefaultErrors;
import fr.skytasul.quests.api.utils.messaging.PlaceholderRegistry;
import fr.skytasul.quests.gui.quests.PlayerListGUI;
import fr.skytasul.quests.gui.quests.QuestsListGUI;
import fr.skytasul.quests.options.OptionStartDialog;
import fr.skytasul.quests.structure.BranchesManagerImplementation;
import fr.skytasul.quests.structure.EndingStageImplementation;
import fr.skytasul.quests.structure.QuestBranchImplementation;
import fr.skytasul.quests.utils.QuestUtils;
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
			for (Quest q : QuestsAPI.getAPI().getQuestsManager().getQuestsStarted(acc)) {
				try {
					q.finish(player);
					success++;
				}catch (Exception ex) {
					QuestsPlugin.getPlugin().getLoggerExpanded()
							.severe("An error occurred while finishing quest " + q.getId(), ex);
					errors++;
				}
			}
			Lang.LEAVE_ALL_RESULT.send(actor.getSender(), PlaceholderRegistry.of("success", success, "errors", errors));
		}
	}
	
	@Subcommand ("finish")
	@CommandPermission ("beautyquests.command.finish")
	public void finish(BukkitCommandActor actor, EntitySelector<Player> players, Quest quest, @Switch boolean force) {
		for (Player player : players) {
			try {
				if (force || quest.hasStarted(PlayersManager.getPlayerAccount(player))) {
					quest.finish(player);
					Lang.LEAVE_ALL_RESULT.send(actor.getSender(), PlaceholderRegistry.of("success", 1, "errors", 0));
				}
			}catch (Exception ex) {
				QuestsPlugin.getPlugin().getLoggerExpanded()
						.severe("An error occurred while finishing quest " + quest.getId(), ex);
				Lang.LEAVE_ALL_RESULT.send(actor.getSender(), PlaceholderRegistry.of("success", 0, "errors", 1));
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
		// syntax: no arg: next or start | 1 arg: start branch | 2 args: set branch stage
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		BranchesManagerImplementation manager = (BranchesManagerImplementation) quest.getBranchesManager();
		
		PlayerQuestDatas datas = acc.getQuestDatasIfPresent(quest);
		if (branchID == null && (datas == null || !datas.hasStarted())) { // start quest
			quest.start(player);
			Lang.START_QUEST.send(actor.getSender(), quest, acc);
			return;
		}
		if (datas == null) datas = acc.getQuestDatas(quest); // creates quest datas
		
		QuestBranchImplementation currentBranch = manager.getBranch(datas.getBranch());
		
		if (branchID == null) { // next
			if (!datas.isInEndingStages()) {
				currentBranch.finishStage(player, currentBranch.getRegularStage(datas.getStage()));
				Lang.COMMAND_SETSTAGE_NEXT.send(actor.getSender());
			}else Lang.COMMAND_SETSTAGE_NEXT_UNAVAILABLE.send(actor.getSender());
		}else {
			QuestBranchImplementation branch = manager.getBranch(branchID);
			if (branch == null)
				throw new CommandErrorException(Lang.COMMAND_SETSTAGE_BRANCH_DOESNTEXIST.quickFormat("branch_id", branchID));
			
			if (stageID != null) {
				if (currentBranch == null)
					throw new CommandErrorException(
							Lang.ERROR_OCCURED.quickFormat("error", "player " + acc.debugName() + " has not started quest"));
				if (branch.getRegularStages().size() <= stageID)
					throw new CommandErrorException(
							Lang.COMMAND_SETSTAGE_STAGE_DOESNTEXIST.quickFormat("stage_id", stageID));
			}
			Lang.COMMAND_SETSTAGE_SET.quickSend(actor.getSender(), "stage_id", stageID);
			if (currentBranch != null) {
				if (datas.isInEndingStages()) {
					for (EndingStage stage : currentBranch.getEndingStages())
						((EndingStageImplementation) stage).getStage().end(acc);
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
			QuestsAPI.getAPI().propagateQuestsHandlers(handler -> handler.questUpdated(acc, quest));
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
				AbstractStage stage =
						quest.getBranchesManager().getBranch(datas.getBranch()).getRegularStage(datas.getStage()).getStage();
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
				Lang.COMMAND_STARTDIALOG_SUCCESS.send(actor.getSender(), acc, quest);
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
			for (@NotNull
			PlayerQuestDatas questDatas : new ArrayList<>(acc.getQuestsDatas())) {
				Quest quest = questDatas.getQuest();
				CompletableFuture<?> future =
						quest == null ? acc.removeQuestDatas(questDatas.getQuestID()) : quest.resetPlayer(acc);
				future = future.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError("An error occurred while resetting quest "
						+ questDatas.getQuestID() + " to player " + player.getName(), actor.getSender()));
				futures.add(future);
				quests++;
			}
			for (@NotNull
			PlayerPoolDatas poolDatas : new ArrayList<>(acc.getPoolDatas())) {
				@Nullable
				QuestPool pool = poolDatas.getPool();
				CompletableFuture<?> future =
						pool == null ? acc.removePoolDatas(poolDatas.getPoolID()) : pool.resetPlayer(acc);
				future = future.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(
						"An error occurred while resetting pool " + poolDatas.getPoolID() + " to player " + player.getName(),
						actor.getSender()));
				futures.add(future);
				pools++;
			}
			acc.resetDatas();

			final int questsFinal = quests;
			final int poolsFinal = pools;
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
					.whenComplete(QuestUtils.runSyncConsumer(() -> {
				Bukkit.getPluginManager().callEvent(new PlayerAccountResetEvent(acc));
				if (acc.isCurrent())
							Lang.DATA_REMOVED.send(player, PlaceholderRegistry.of("quest_amount", questsFinal,
									"deleter_name", actor.getName(), "pool_amount", poolsFinal));
						Lang.DATA_REMOVED_INFO.send(actor.getSender(), PlaceholderRegistry.of("quest_amount", questsFinal,
								"player_name", player.getName(), "pool_amount", poolsFinal));
			}));

		}
	}
	
	@Subcommand ("resetPlayerQuest")
	@CommandPermission ("beautyquests.command.resetPlayer")
	public void resetPlayerQuest(BukkitCommandActor actor, Player player, @Optional Quest quest) {
		if (quest != null) {
			reset(actor.getSender(), player, quest);
		}else {
			new QuestsListGUI(obj -> {
				reset(actor.getSender(), player, obj);
			}, PlayersManager.getPlayerAccount(player), true, false, true).open(actor.requirePlayer());
		}
	}
	
	private void reset(CommandSender sender, Player target, Quest qu) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(target);
		qu.resetPlayer(acc).whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(__ -> {
			if (acc.isCurrent())
				Lang.DATA_QUEST_REMOVED.send(target, qu.getPlaceholdersRegistry(),
						PlaceholderRegistry.of("deleter_name", sender.getName()));
			Lang.DATA_QUEST_REMOVED_INFO.send(sender, acc, qu);
		}, "An error occurred while removing player quest data", sender));
	}
	
	@Subcommand ("resetQuest")
	@CommandPermission ("beautyquests.command.resetQuest")
	public void resetQuest(BukkitCommandActor actor, Quest quest) {
		List<CompletableFuture<Boolean>> futures = new ArrayList<>(Bukkit.getOnlinePlayers().size());

		for (Player p : Bukkit.getOnlinePlayers()) {
			futures.add(quest.resetPlayer(PlayersManager.getPlayerAccount(p))
					.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(
							"An error occurred while resetting quest " + quest.getId() + " to player " + p.getName(),
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
					.whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError(removedAmount -> {
						Lang.QUEST_PLAYERS_REMOVED.quickSend(actor.getSender(), "player_amount",
								removedAmount + resetAmount);
					}, "An error occurred while removing quest datas", actor.getSender()));
		}).whenComplete(QuestsPlugin.getPlugin().getLoggerExpanded().logError());

	}
	
	@Subcommand ("seePlayer")
	@CommandPermission ("beautyquests.command.seePlayer")
	public void seePlayer(Player actor, Player player) {
		new PlayerListGUI(BeautyQuests.getInstance().getPlayersManager().getAccount(player), false).open(actor);
	}
	
	@Subcommand ("start")
	@CommandPermission ("beautyquests.command.start")
	public void start(BukkitCommandActor actor, ExecutableCommand command, EntitySelector<Player> players,
			@Optional Quest quest,
			@CommandPermission("beautyquests.command.start.other") @Switch boolean overrideRequirements) {
		if (actor.isPlayer() && !startOtherPermission.canExecute(actor)) {
			if (players.isEmpty() || players.size() > 1 || (players.get(0) != actor.getAsPlayer()))
				throw new NoPermissionException(command, startOtherPermission);
		}
		
		for (Player player : players) {
			if (quest == null) {
				new QuestsListGUI(obj -> {
					start(actor.getSender(), player, obj, overrideRequirements);
				}, PlayersManager.getPlayerAccount(player), false, true, false).open(actor.requirePlayer());
			}else {
				start(actor.getSender(), player, quest, overrideRequirements);
			}
		}
	}

	private void start(CommandSender sender, Player player, Quest quest, boolean overrideRequirements) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(player);
		if (!overrideRequirements && !quest.canStart(player, true)) {
			Lang.START_QUEST_NO_REQUIREMENT.send(sender, quest, acc);
			return;
		}
		quest.start(player);
		Lang.START_QUEST.send(sender, quest, acc);
	}
	
	@Subcommand ("cancel")
	@CommandPermission ("beautyquests.command.cancel")
	public void cancel(BukkitCommandActor actor, ExecutableCommand command, EntitySelector<Player> players,
			@Optional Quest quest) {
		if (actor.isPlayer() && !cancelOtherPermission.canExecute(actor)) {
			if (players.isEmpty() || players.size() > 1 || (players.get(0) != actor.getAsPlayer()))
				throw new NoPermissionException(command, cancelOtherPermission);
		}
		
		for (Player player : players) {
			PlayerAccount acc = PlayersManager.getPlayerAccount(player);
			
			if (quest == null) {
				new QuestsListGUI(obj -> {
					cancel(actor.getSender(), acc, obj);
				}, acc, true, false, false).open(actor.requirePlayer());
			}else {
				cancel(actor.getSender(), acc, quest);
			}
		}
	}
	
	private void cancel(CommandSender sender, PlayerAccount acc, Quest quest) {
		if (!quest.isCancellable()) {
			Lang.CANCEL_QUEST_UNAVAILABLE.send(sender, quest);
			return;
		}

		if (quest.cancelPlayer(acc)) {
			Lang.CANCEL_QUEST.send(sender, quest);
		} else {
			if (sender.equals(acc.getPlayer())) {
				Lang.QUEST_NOT_STARTED.send(sender);
			} else {
				DefaultErrors.sendGeneric(sender,
						"Player " + acc.getName() + " does not have the quest " + quest.getId() + " started.");
			}
		}
	}
	
}
