package fr.skytasul.quests.api.npcs.dialogs;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.npcs.BqNpc;

public interface DialogRunner {

	@Nullable
	BqNpc getNpc();

	@Nullable
	Dialog getDialog();

	void addTest(Predicate<Player> test);

	void addTestCancelling(Predicate<Player> test);

	void addEndAction(Consumer<Player> action);

	/**
	 * Tests if the player is close enough to the NPC for the dialog to continue.
	 * <p>
	 * This is <i>not</i> tested on player click, only for automatic messages with durations.
	 * @param p Player to check the distance from the NPC
	 * @return <code>true</code> if the player is close enough to the NPC or if the distance feature is disabled, <code>false</code> otherwise.
	 */
	boolean canContinue(Player p);

	/**
	 * Must be called when the player clicks on the NPC.
	 * This will send the dialog to the player if conditions are met.
	 * @param p player which
	 * @return the result of tests to run this dialog
	 */
	TestResult onClick(Player p);

	TestResult handleNext(Player p, DialogNextReason reason);

	boolean isPlayerInDialog(Player p);

	int getPlayerMessage(Player p);

	boolean removePlayer(Player player);

	/**
	 * Forces the end of the dialog, toggles the end actions and removes the player from this dialog
	 * runner.
	 *
	 * @param player player to end the dialog for
	 */
	void forceFinish(@NotNull Player player);

	public enum DialogNextReason {
		NPC_CLICK, AUTO_TIME, COMMAND, PLUGIN;
	}

	public enum TestResult {
		ALLOW, DENY, DENY_CANCEL;

		public TestResult accumulate(TestResult other) {
			if (this == DENY_CANCEL || other == DENY_CANCEL)
				return DENY_CANCEL;
			if (this == DENY || other == DENY)
				return DENY;
			return ALLOW;
		}

		public boolean shouldCancel() {
			return this == DENY_CANCEL || this == ALLOW;
		}
	}

}
