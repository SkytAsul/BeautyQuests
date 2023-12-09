package fr.skytasul.quests.utils.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.SmithItemEvent;
import fr.skytasul.quests.api.events.internal.BQCraftEvent;

public class Post1_16 implements Listener {
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSmith(SmithItemEvent event) {
		Bukkit.getPluginManager().callEvent(new BQCraftEvent(event, event.getCurrentItem(), event.getCurrentItem().getAmount()));
	}
	
}
