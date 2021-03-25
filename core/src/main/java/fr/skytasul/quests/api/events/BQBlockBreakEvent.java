package fr.skytasul.quests.api.events;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class BQBlockBreakEvent extends PlayerEvent {
	
	private final List<Block> blocks;
	
	public BQBlockBreakEvent(Player player, List<Block> blocks) {
		super(player);
		Validate.notNull(player);
		Validate.notNull(blocks);
		this.blocks = blocks;
	}
	
	public List<Block> getBlocks() {
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
