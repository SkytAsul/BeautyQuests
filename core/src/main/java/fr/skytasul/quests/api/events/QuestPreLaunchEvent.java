package fr.skytasul.quests.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import fr.skytasul.quests.structure.Quest;

/**
 * Called before a player starts a quest
 */
public class QuestPreLaunchEvent extends PlayerQuestEvent implements Cancellable{

	private boolean cancel = false;
	
	public QuestPreLaunchEvent(@NotNull Player who, @NotNull Quest quest) {
		super(who, quest);
	}

	@Override
	public boolean isCancelled(){
		return cancel;
	}

	@Override
	public void setCancelled(boolean paramBoolean){
		this.cancel = paramBoolean;
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
