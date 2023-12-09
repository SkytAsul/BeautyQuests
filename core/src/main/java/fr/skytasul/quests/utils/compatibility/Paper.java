package fr.skytasul.quests.utils.compatibility;

import java.util.Iterator;
import java.util.function.Predicate;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import fr.skytasul.quests.api.utils.MinecraftVersion;

public final class Paper {
	
	private Paper() {}
	
	public static void handleDeathItems(PlayerDeathEvent event, Predicate<ItemStack> predicate) {
		if (MinecraftVersion.MAJOR < 13) return;
		
		for (Iterator<ItemStack> iterator = event.getDrops().iterator(); iterator.hasNext();) {
			ItemStack item = iterator.next();
			if (predicate.test(item)) {
				iterator.remove();
				event.getItemsToKeep().add(item);
			}
		}
	}
	
}
