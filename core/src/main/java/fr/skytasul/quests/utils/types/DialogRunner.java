package fr.skytasul.quests.utils.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.events.DialogSendEvent;
import fr.skytasul.quests.api.events.DialogSendMessageEvent;
import fr.skytasul.quests.api.npcs.BQNPC;
import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.Lang;

public class DialogRunner {
	
	private final Dialog dialog;
	private final BQNPC npc;
	
	private List<Predicate<Player>> tests = new ArrayList<>();
	private List<Predicate<Player>> testsCancelling = new ArrayList<>();
	private List<Consumer<Player>> endActions = new ArrayList<>();
	
	private Map<Player, PlayerStatus> players = new HashMap<>();
	private Boolean navigationInitiallyPaused = null;
	
	public DialogRunner(Dialog dialog, BQNPC npc) {
		this.dialog = dialog;
		this.npc = npc;
	}
	
	public void addTest(Predicate<Player> test) {
		tests.add(test);
	}
	
	public void addTestCancelling(Predicate<Player> test) {
		testsCancelling.add(test);
	}
	
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
	
	/**
	 * Tests if the player is close enough to the NPC for the dialog to continue.
	 * <p>
	 * This is <i>not</i> tested on player click, only for automatic messages with durations.
	 * @param p Player to check the distance from the NPC
	 * @return <code>true</code> if the player is close enough to the NPC or if the distance feature is disabled, <code>false</code> otherwise.
	 */
	public boolean canContinue(Player p) {
		if (npc == null || QuestsConfiguration.getDialogsConfig().getMaxDistance() == 0) return true;
		return p.getLocation().distanceSquared(npc.getLocation()) <= QuestsConfiguration.getDialogsConfig().getMaxDistanceSquared();
	}
	
	private void end(Player p) {
		if (test(p) != TestResult.ALLOW) {
			BeautyQuests.logger.warning("Dialog predicates not completed for NPC " + npc.getId()
					+ " whereas the dialog should end. This is a bug.");
			return;
		}
		
		endActions.forEach(x -> x.accept(p));
	}
	
	/**
	 * Must be called when the player clicks on the NPC.
	 * This will send the dialog to the player if conditions are met.
	 * @param p player which
	 * @return the result of tests to run this dialog
	 */
	public TestResult onClick(Player p) {
		if (QuestsConfiguration.getDialogsConfig().isClickDisabled()) {
			PlayerStatus status = players.get(p);
			if (status != null && status.task != null) return TestResult.DENY;
		}
		
		if (p.isSneaking() && dialog != null && dialog.isSkippable() && test(p) == TestResult.ALLOW) {
			Lang.DIALOG_SKIPPED.sendWP(p);
			removePlayer(p);
			end(p);
			return TestResult.ALLOW;
		}
		return handleNext(p, DialogNextReason.NPC_CLICK);
	}
	
	public TestResult handleNext(Player p, DialogNextReason reason) {
		TestResult test = test(p);
		if (test == TestResult.ALLOW) {
			// player fulfills conditions to start or continue the dialog
			
			if (dialog == null || npc == null) {
				end(p);
				return TestResult.ALLOW;
			}
			
			DialogSendEvent event = new DialogSendEvent(dialog, npc, p, () -> end(p));
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
				Message message = dialog.messages.get(status.lastId);
				if (message.getWaitTime() != 0) {
					status.task = Bukkit.getScheduler().runTaskLater(BeautyQuests.getInstance(), () -> {
						status.task = null;
						// we test if the player is within the authorized distance from the NPC
						if (canContinue(p)) {
							handleNext(p, DialogNextReason.AUTO_TIME);
						}else {
							Lang.DIALOG_TOO_FAR.send(p, dialog.getNPCName(npc));
							removePlayer(p);
						}
					}, message.getWaitTime());
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
		if (dialog.messages.isEmpty()) return true;
		
		int id = ++status.lastId;
		boolean endOfDialog = id == dialog.messages.size();
		
		if (status.runningMsg != null)
			status.runningMsg.finished(p, endOfDialog, reason != DialogNextReason.AUTO_TIME);
		if (status.runningMsgTask != null) status.runningMsgTask.cancel();
		
		if (endOfDialog) return true;
		
		Message msg = dialog.messages.get(id);
		if (msg == null) {
			p.sendMessage("§cMessage with ID " + id + " does not exist. Please report this to an adminstrator. Method caller: " + DebugUtils.stackTraces(2, 3));
			return true;
		}
		
		status.runningMsg = msg;
		DialogSendMessageEvent event = new DialogSendMessageEvent(dialog, msg, npc, p);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled())
			status.runningMsgTask = msg.sendMessage(p, dialog.getNPCName(npc), id, dialog.messages.size());
		
		return false;
	}
	
	public boolean isPlayerInDialog(Player p) {
		return players.containsKey(p);
	}
	
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
			navigationInitiallyPaused = npc.setNavigationPaused(true);
		}
		return status;
	}
	
	public boolean removePlayer(Player player) {
		PlayerStatus status = players.remove(player);
		if (status == null) return false;
		status.cancel();
		
		handlePlayerChanges();
		
		return true;
	}
	
	private void handlePlayerChanges() {
		if (players.isEmpty() && npc != null && navigationInitiallyPaused != null) {
			// if no more players are in dialog, resume NPC walking
			npc.setNavigationPaused(navigationInitiallyPaused);
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
		BukkitTask task = null;
		BukkitTask runningMsgTask = null;
		Message runningMsg = null;
		
		void cancel() {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}
	
	public enum DialogNextReason {
		NPC_CLICK, AUTO_TIME, COMMAND, PLUGIN;
	}

	public enum TestResult {
		ALLOW, DENY, DENY_CANCEL;
		
		public TestResult accumulate(TestResult other) {
			if (this == DENY_CANCEL || other == DENY_CANCEL) return DENY_CANCEL;
			if (this == DENY || other == DENY) return DENY;
			return ALLOW;
		}
		
		public boolean shouldCancel() {
			return this == DENY_CANCEL || this == ALLOW;
		}
	}
	
}
