package fr.skytasul.quests.integrations;

import com.songoda.ultimatetimber.api.event.TreeFallEvent;
import com.songoda.ultimatetimber.api.tree.TreeBlock;
import com.songoda.ultimatetimber.api.tree.TreeBlockSet;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.events.internal.BQBlockBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UltimateTimber fires {@code TreeFellEvent} only once the felling animation has finished, at which point the logs have
 * already been removed from the world. {@link TreeFallEvent} is fired before the tree topples, so the logs are still in
 * place for the mine stages to check their type.
 */
public class BQUltimateTimber implements Listener {

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTreeFall(TreeFallEvent e) {
		TreeBlockSet<Block> blocks = e.getDetectedTree().detectedTreeBlocks();
		TreeBlock<Block> initialLog = isInitialBreakAlreadyCounted() ? blocks.getInitialLogBlock() : null;

		List<Block> logs = blocks.getLogBlocks()
				.stream()
				.filter(log -> !log.equals(initialLog))
				.map(TreeBlock::block)
				.collect(Collectors.toList());

		if (!logs.isEmpty())
			Bukkit.getPluginManager().callEvent(new BQBlockBreakEvent(e.getPlayer(), logs));
	}

	/**
	 * The first log is broken by the player. UltimateTimber cancels that {@link BlockBreakEvent} at
	 * {@link EventPriority#HIGHEST} when the tree falls, and BeautyQuests only counts non-cancelled breaks at the same
	 * priority. Which of both handlers runs first depends on the plugin load order, so it is looked up here: if
	 * BeautyQuests' handler runs first, the initial log has already been counted.
	 */
	private static boolean isInitialBreakAlreadyCounted() {
		Plugin quests = QuestsPlugin.getPlugin();
		for (RegisteredListener listener : BlockBreakEvent.getHandlerList().getRegisteredListeners()) {
			if (listener.getPriority() != EventPriority.HIGHEST)
				continue;
			if (listener.getPlugin() == quests)
				return true;
			if (listener.getPlugin().getName().equals("UltimateTimber"))
				return false;
		}
		return false;
	}

}
