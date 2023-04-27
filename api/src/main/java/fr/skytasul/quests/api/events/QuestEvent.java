package fr.skytasul.quests.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.api.quests.Quest;

public abstract class QuestEvent extends Event {

	protected final @NotNull Quest quest;
	
	protected QuestEvent(@NotNull Quest quest) {
		this.quest = quest;
	}

	public @NotNull Quest getQuest() {
		return quest;
	}

	@Override
	public HandlerList getHandlers(){
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	  return handlers;
	}

	private static final HandlerList handlers = new HandlerList();

}
