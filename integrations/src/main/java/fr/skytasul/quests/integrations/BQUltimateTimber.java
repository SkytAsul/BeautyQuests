package fr.skytasul.quests.integrations;

import com.songoda.ultimatetimber.api.event.TreeFallEvent;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import fr.skytasul.quests.api.events.internal.BQBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import java.util.stream.Collectors;

/**
 * UltimateTimber fires {@code TreeFellEvent} only once the felling animation has finished, at which point the logs have
 * already been removed from the world. {@link TreeFallEvent} is fired before the tree topples, so the logs are still in
 * place for the mine stages to check their type.
 */
public class BQUltimateTimber implements Listener {

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTreeFall(TreeFallEvent e) {
		Bukkit.getPluginManager().callEvent(new BQBlockBreakEvent(e.getPlayer(),
				e.getDetectedTree()
				.detectedTreeBlocks()
				.getLogBlocks()
				.stream()
				.map(TreeBlock<Block>::block)
				.collect(Collectors.toList())));
	}

}
