package fr.skytasul.quests.api.questers.events;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractQuesterEvent extends Event {

	protected final @NotNull Quester quester;

	protected AbstractQuesterEvent(@NotNull Quester quester) {
		this.quester = quester;
	}

	public @NotNull Quester getQuester() {
		return quester;
	}

}