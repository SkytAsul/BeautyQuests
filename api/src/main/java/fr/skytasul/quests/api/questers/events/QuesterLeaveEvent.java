package fr.skytasul.quests.api.questers.events;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuesterLeaveEvent extends AbstractQuesterEvent {

	private final @NotNull Player player;

	public QuesterLeaveEvent(@NotNull Quester quester, @NotNull Player player) {
		super(quester);
		this.player = player;
	}

	public @NotNull Player getPlayer() {
		return player;
	}

	public boolean isLastOnlinePlayer() {
		return quester.getOnlinePlayers().size() == 1;
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
