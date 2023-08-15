package fr.skytasul.quests.integrations;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import fr.skytasul.quests.api.events.internal.BQBlockBreakEvent;

public class BQTokenEnchant implements Listener {
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onTEBlockExplode(TEBlockExplodeEvent e) {
		if (e.isCancelled()) return;
		if (e.getPlayer() == null) return;
		Bukkit.getPluginManager().callEvent(new BQBlockBreakEvent(e.getPlayer(), e.blockList()));
	}
	
}
