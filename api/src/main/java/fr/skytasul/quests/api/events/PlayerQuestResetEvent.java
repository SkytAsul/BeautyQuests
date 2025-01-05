package fr.skytasul.quests.api.events;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;

/**
 * Called when the stage of a player is cancelled
 */
public class PlayerQuestResetEvent extends PlayerQuestEvent {

	public PlayerQuestResetEvent(@NotNull Quester account, @NotNull Quest quest) {
		super(account, quest);
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
