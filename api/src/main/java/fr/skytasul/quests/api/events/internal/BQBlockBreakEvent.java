package fr.skytasul.quests.api.events.internal;

import java.util.List;
import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class BQBlockBreakEvent extends PlayerEvent {
	
	private final @NotNull List<@NotNull Block> blocks;
	
	public BQBlockBreakEvent(@NotNull Player player, @NotNull List<@NotNull Block> blocks) {
		super(player);
		Validate.notNull(player);
		Validate.notNull(blocks);
		this.blocks = blocks;
	}
	
	public @NotNull List<@NotNull Block> getBlocks() {
		return blocks;
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
