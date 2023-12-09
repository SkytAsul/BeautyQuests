package fr.skytasul.quests.api.gui.close;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * This close behavior will remove the player from the GUI system and execute the runnable on the
 * next tick.
 */
public class DelayCloseBehavior implements CloseBehavior {

	private final @NotNull Runnable delayed;

	public DelayCloseBehavior(@NotNull Runnable delayed) {
		this.delayed = Objects.requireNonNull(delayed);
	}

	public @NotNull Runnable getDelayed() {
		return delayed;
	}

}
