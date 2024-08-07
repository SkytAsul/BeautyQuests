package fr.skytasul.quests.integrations.mobs;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.mobs.MobStacker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import uk.antiperson.stackmob.events.StackDeathEvent;
import java.util.concurrent.TimeUnit;

public class BQStackMob implements Listener, MobStacker {

	private final Cache<Integer, Integer> killedStacks =
			CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

	private BQStackMob() {}

	public static void initialize() {
		BQStackMob stacker = new BQStackMob();
		QuestsAPI.getAPI().registerMobStacker(stacker);
		Bukkit.getPluginManager().registerEvents(stacker, QuestsPlugin.getPlugin());
	}

	@EventHandler
	public void onStackDeath(StackDeathEvent event) {
		killedStacks.put(event.getStackEntity().getEntity().getEntityId(), event.getDeathStep());
	}

	@Override
	public int getEntityStackSize(@NotNull Entity entity) {
		Integer amount = killedStacks.getIfPresent(entity.getEntityId());
		return amount == null ? 1 : amount.intValue();
	}

}
