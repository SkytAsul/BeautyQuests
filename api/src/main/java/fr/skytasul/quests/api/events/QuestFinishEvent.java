package fr.skytasul.quests.api.events;

import fr.skytasul.quests.api.players.Quester;
import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player finish a quest
 */
public class QuestFinishEvent extends PlayerQuestEvent{

	public QuestFinishEvent(@NotNull Quester who, @NotNull Quest quest) {
		super(who, quest);
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
