package fr.skytasul.quests.api.events.internal;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BeautyQuestsLoadedEvent extends Event {

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
