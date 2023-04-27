package fr.skytasul.quests.api.gui.close;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.CustomInventory;

public class OpenCloseBehavior implements CloseBehavior {

	private final @NotNull CustomInventory other;

	public OpenCloseBehavior(@NotNull CustomInventory other) {
		this.other = Objects.requireNonNull(other);
	}

	public @NotNull CustomInventory getOther() {
		return other;
	}

}
