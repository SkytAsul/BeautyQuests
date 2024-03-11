package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import fr.euphyllia.energie.model.SchedulerTaskInter;
import fr.euphyllia.energie.model.SchedulerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.QuestsConfiguration;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.DialogSendEvent;
import fr.skytasul.quests.api.events.DialogSendMessageEvent;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.npcs.BqNpc;
import fr.skytasul.quests.api.npcs.dialogs.Dialog;
import fr.skytasul.quests.api.npcs.dialogs.DialogRunner;
import fr.skytasul.quests.api.npcs.dialogs.Message;
import fr.skytasul.quests.utils.DebugUtils;

public class DialogRunnerImplementation implements DialogRunner {

	private final @Nullable Dialog dialog;
	private final @Nullable BqNpc npc;

	private List<Predicate<Player>> tests = new ArrayList<>();
	private List<Predicate<Player>> testsCancelling = new ArrayList<>();
	private List<Consumer<Player>> endActions = new ArrayList<>();

	private Map<Player, PlayerStatus> players = new HashMap<>();
	private Boolean navigationInitiallyPaused = null;

	public DialogRunnerImplementation(@Nullable Dialog dialog, @Nullable BqNpc npc) {
		this.dialog = dialog;
		this.npc = npc;
	}

	public @Nullable BqNpc getNpc() {
		return npc;
	}

	public @Nullable Dialog getDialog() {
		return dialog;
	}

	@Override
	public void addTest(Predicate<Player> test) {
		tests.add(test);
	}

	@Override
	public void addTestCancelling(Predicate<Player> test) {
		testsCancelling.add(test);
	}

	@Override
	public void addEndAction(Consumer<Player> action) {
		endActions.add(action);
	}

	private TestResult test(Player p) {
		if (!tests.stream().allMatch(x -> x.test(p)))
			return TestResult.DENY;
		if (!testsCancelling.stream().allMatch(x -> x.test(p)))
			return TestResult.DENY_CANCEL;
		return TestResult.ALLOW;
	}

	@Override
	public boolean canContinue(Player p) {
		if (npc == null || QuestsConfiguration.getConfig().getDialogsConfig().getMaxDistance() == 0)
			return true;
		return p.getLocation().distanceSquared(npc.getLocation()) <= QuestsConfiguration.getConfig().getDialogsConfig()
				.getMaxDistanceSquared();
	}

	private void end(Player p) {
		if (test(p) != TestResult.ALLOW) {
			QuestsPlugin.getPlugin().getLoggerExpanded()
					.warning("Dialog predicates not completed for NPC " + npc.getId()
							+ " whereas the dialog should end. This is a bug.");
			return;
		}

		endActions.forEach(x -> x.accept(p));
	}

	@Override
	public TestResult onClick(Player p) {
		if (QuestsConfiguration.getConfig().getDialogsConfig().isClickDisabled()) {
			PlayerStatus status = players.get(p);
			if (status != null && status.task != null) return TestResult.DENY;
		}

		if (p.isSneaking() && dialog != null && dialog.isSkippable() && test(p) == TestResult.ALLOW) {
			Lang.DIALOG_SKIPPED.send(p);
			removePlayer(p);
			end(p);
			return TestResult.ALLOW;
		}
		return handleNext(p, DialogNextReason.NPC_CLICK);
	}

	@Override
	public TestResult handleNext(Player p, DialogNextReason reason) {
		TestResult test = test(p);
		if (test == TestResult.ALLOW) {
			// player fulfills conditions to start or continue the dialog

			if (dialog == null || npc == null) {
				end(p);
				return TestResult.ALLOW;
			}

			DialogSendEvent event = new DialogSendEvent(p, this);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) return TestResult.DENY_CANCEL;

			PlayerStatus status = addPlayer(p);
			status.cancel();

			if (send(p, status, reason)) {
				// when dialog finished
				removePlayer(p);
				end(p);
			}else {
				// when dialog not finished, launch task if needed
				Message message = dialog.getMessages().get(status.lastId);
				if (message.getWaitTime() != 0) {
					status.task = null;
					QuestsPlugin.getPlugin().getScheduler().runDelayed(SchedulerType.SYNC, p, taskInter -> {
						status.task = taskInter;
						// we test if the player is within the authorized distance from the NPC
						if (canContinue(p)) {
							handleNext(p, DialogNextReason.AUTO_TIME);
						}else {
							Lang.DIALOG_TOO_FAR.quickSend(p, "npc_name", dialog.getNPCName(npc));
							removePlayer(p);
						}
					}, null, message.getWaitTime());
				}
			}
			return TestResult.ALLOW;
		}else {
			// if the player does not fulfills the conditions to continue the dialog,
			// we remove it immediately to cancel the dialog started status
			removePlayer(p);
			return test;
		}
	}

	/**
	 * Sends the next dialog line for a player, or the first message if it has just begun the dialog.
	 *
	 * @param p player to send the dialog to
	 * @param reason reason the message has to be sent
	 * @return <code>true</code> if the dialog ends following this call, <code>false</code>otherwise
	 */
	private boolean send(Player p, PlayerStatus status, DialogNextReason reason) {
		if (dialog.getMessages().isEmpty())
			return true;

		int id = ++status.lastId;
		boolean endOfDialog = id == dialog.getMessages().size();

		if (status.runningMsg != null)
			status.runningMsg.finished(p, endOfDialog, reason != DialogNextReason.AUTO_TIME);
		if (status.runningMsgTask != null) status.runningMsgTask.cancel();

		if (endOfDialog) return true;

		Message msg = dialog.getMessages().get(id);
		if (msg == null) {
			p.sendMessage("§cMessage with ID " + id + " does not exist. Please report this to an adminstrator. Method caller: " + DebugUtils.stackTraces(2, 3));
			return true;
		}

		status.runningMsg = msg;
		DialogSendMessageEvent event = new DialogSendMessageEvent(p, this, msg);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled())
			status.runningMsgTask = msg.sendMessage(p, npc, dialog.getNPCName(npc), id, dialog.getMessages().size());

		return false;
	}

	@Override
	public boolean isPlayerInDialog(Player p) {
		return players.containsKey(p);
	}

	@Override
	public int getPlayerMessage(Player p) {
		return players.get(p).lastId;
	}

	public PlayerStatus addPlayer(Player player) {
		PlayerStatus status = players.get(player);
		if (status != null) return status;

		status = new PlayerStatus();
		players.put(player, status);

		if (npc != null && navigationInitiallyPaused == null) {
			// pause NPC walking as there is a player in dialog
			navigationInitiallyPaused = npc.getNpc().setNavigationPaused(true);
		}
		return status;
	}

	@Override
	public boolean removePlayer(Player player) {
		PlayerStatus status = players.remove(player);
		if (status == null) return false;
		status.cancel();

		handlePlayerChanges();

		return true;
	}

	@Override
	public void forceFinish(@NotNull Player player) {
		end(player);
		removePlayer(player);
	}

	private void handlePlayerChanges() {
		if (players.isEmpty() && npc != null && navigationInitiallyPaused != null) {
			// if no more players are in dialog, resume NPC walking
			npc.getNpc().setNavigationPaused(navigationInitiallyPaused);
			navigationInitiallyPaused = null;
		}
	}

	public void unload() {
		if (!players.isEmpty()) players.values().forEach(PlayerStatus::cancel);
		players.clear();
		handlePlayerChanges();
	}

	class PlayerStatus {
		int lastId = -1;
		SchedulerTaskInter task = null;
		SchedulerTaskInter runningMsgTask = null;
		Message runningMsg = null;

		void cancel() {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}

}
