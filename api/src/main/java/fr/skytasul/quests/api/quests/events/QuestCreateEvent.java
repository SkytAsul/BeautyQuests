package fr.skytasul.quests.api.quests.events;

import fr.skytasul.quests.api.quests.Quest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Called when a quest is created
 */
public class QuestCreateEvent extends AbstractQuestEvent implements Cancellable {

	private final boolean edited;
	private final @Nullable Player creator;

	private boolean cancel;

	public QuestCreateEvent(@NotNull Quest quest, boolean edited, @Nullable Player creator) {
		super(quest);
		this.edited = edited;
		this.creator = creator;
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
		return edited;
	}

	public @Nullable Player getCreator() {
		return creator;
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
