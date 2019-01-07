package fr.skytasul.quests.utils.compatibility.mobs;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.songoda.epicbosses.CustomBosses;
import com.songoda.epicbosses.events.BossDeathEvent;
import com.songoda.epicbosses.utils.EntityFinder;

public class EpicBosses implements Listener {

	public static boolean bossExists(String name){
		return CustomBosses.get().getBossEntityContainer().exists(name);
	}
	
	public static EntityType getBossEntityType(String name){
		return EntityFinder.get(CustomBosses.get().getBossEntityContainer().getData().get(name).getEntityStats().get(0).getMainStats().getEntityType().split(":")[0]).getEntityType();
	}
	
	@EventHandler
	public void onBossDeath(BossDeathEvent e){
		LivingEntity en = e.getActiveBossHolder().getLivingEntity();
		if (en.getKiller() == null) return;
		Bukkit.getPluginManager().callEvent(new CompatMobDeathEvent(e.getActiveBossHolder().getName(), en.getKiller(), en));
	}
	
}