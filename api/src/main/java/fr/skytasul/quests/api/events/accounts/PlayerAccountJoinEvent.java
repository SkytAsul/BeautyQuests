package fr.skytasul.quests.api.events.accounts;

import fr.skytasul.quests.api.players.Quester;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerAccountJoinEvent extends PlayerAccountEvent {

	private final Player player;
	private final boolean firstJoin;

	public PlayerAccountJoinEvent(@NotNull Quester account, @NotNull Player player, boolean firstJoin) {
		super(account);
		this.player = player;
		this.firstJoin = firstJoin;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public boolean isFirstJoin() {
		return firstJoin;
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
