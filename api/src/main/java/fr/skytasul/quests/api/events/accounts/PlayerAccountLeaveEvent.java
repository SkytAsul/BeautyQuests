package fr.skytasul.quests.api.events.accounts;

import fr.skytasul.quests.api.players.Quester;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerAccountLeaveEvent extends PlayerAccountEvent {

	private final @NotNull Player player;

	public PlayerAccountLeaveEvent(@NotNull Quester account, @NotNull Player player) {
		super(account);
		this.player = player;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
