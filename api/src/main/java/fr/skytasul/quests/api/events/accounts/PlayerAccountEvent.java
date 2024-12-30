package fr.skytasul.quests.api.events.accounts;

import fr.skytasul.quests.api.players.Quester;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public abstract class PlayerAccountEvent extends Event {

	protected final @NotNull Quester account;

	protected PlayerAccountEvent(@NotNull Quester account) {
		this.account = account;
	}

	public @NotNull Quester getQuester() {
		return account;
	}

}