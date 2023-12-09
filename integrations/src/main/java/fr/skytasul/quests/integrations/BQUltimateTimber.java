package fr.skytasul.quests.integrations;

import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.songoda.ultimatetimber.events.TreeFellEvent;
import com.songoda.ultimatetimber.tree.ITreeBlock;
import fr.skytasul.quests.api.events.internal.BQBlockBreakEvent;

public class BQUltimateTimber implements Listener {
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onTreeFell(TreeFellEvent e) {
		Bukkit.getPluginManager().callEvent(new BQBlockBreakEvent(e.getPlayer(),
				e.getDetectedTree()
				.getDetectedTreeBlocks()
				.getLogBlocks()
				.stream()
				.map(ITreeBlock<Block>::getBlock)
				.collect(Collectors.toList())));
	}
	
}
