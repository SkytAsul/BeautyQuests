package fr.skytasul.quests.api.questers.events;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuesterJoinEvent extends AbstractQuesterEvent {

	private final Player player;
	private final boolean firstJoin;

	public QuesterJoinEvent(@NotNull Quester quester, @NotNull Player player, boolean firstJoin) {
		super(quester);
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
