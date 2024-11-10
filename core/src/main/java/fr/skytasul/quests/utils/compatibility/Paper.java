package fr.skytasul.quests.utils.compatibility;

import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import java.util.function.Predicate;

public interface Paper {

	public void keepDeathItems(PlayerDeathEvent event, Predicate<ItemStack> predicate);

}
