package fr.skytasul.quests.api.gui.close;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.gui.Gui;

public class OpenCloseBehavior implements CloseBehavior {

	private final @NotNull Gui other;

	public OpenCloseBehavior(@NotNull Gui other) {
		this.other = Objects.requireNonNull(other);
	}

	public @NotNull Gui getOther() {
		return other;
	}

}
