package fr.skytasul.quests.api.quests.events.questers;

import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player starts a quest
 */
public class QuesterQuestPreLaunchEvent extends AbstractQuesterQuestEvent implements Cancellable{

	private boolean cancel = false;

	public QuesterQuestPreLaunchEvent(@NotNull Quester quester, @NotNull Quest quest) {
		super(quester, quest);
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
