package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.structure.Quest;

/**
 * Called when a quest is created
 */
public class QuestCreateEvent extends PlayerQuestEvent implements Cancellable{

	private boolean cancel, edit = false;
	
	public QuestCreateEvent(@NotNull Player who, @NotNull Quest quest, boolean edit) {
		super(who, quest);
		this.edit = edit;
	}

	@Override
	public boolean isCancelled(){
		return cancel;
	}

	@Override
	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
	}
	
	public boolean isEdited(){
		return edit;
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
