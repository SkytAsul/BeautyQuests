package fr.skytasul.quests.api.events.accounts;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

// TODO rename to quester names
public abstract class PlayerAccountEvent extends Event {

	protected final @NotNull Quester account;

	protected PlayerAccountEvent(@NotNull Quester account) {
		this.account = account;
	}

	public @NotNull Quester getQuester() {
		return account;
	}

}