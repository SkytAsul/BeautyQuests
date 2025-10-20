package fr.skytasul.quests.api.questers.events;

import fr.skytasul.quests.api.questers.Quester;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class QuesterResetEvent extends AbstractQuesterEvent {

	public QuesterResetEvent(@NotNull Quester quester) {
		super(quester);
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
