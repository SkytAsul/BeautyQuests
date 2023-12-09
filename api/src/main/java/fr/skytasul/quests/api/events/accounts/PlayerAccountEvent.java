package fr.skytasul.quests.api.events.accounts;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import fr.skytasul.quests.api.players.PlayerAccount;

public abstract class PlayerAccountEvent extends Event {
	
	protected final @NotNull PlayerAccount account;

	protected PlayerAccountEvent(@NotNull PlayerAccount account) {
		this.account = account;
	}
	
	public boolean isAccountCurrent() {
		return account.isCurrent();
	}
	
	public @Nullable Player getPlayer() {
		if (!account.isCurrent())
			throw new IllegalStateException("Account is not currently used");

		return account.getPlayer();
	}

	public @NotNull PlayerAccount getPlayerAccount() {
		return account;
	}
	
}