package fr.skytasul.quests.integrations.mobs;

import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class EpicBosses implements Listener {

	public static boolean bossExists(String name){
		throw new RuntimeException("EpicBosses compatibility temporarily disabled");
		//return CustomBosses.get().getBossEntityContainer().exists(name);
	}
	
	public static EntityType getBossEntityType(String name){
		throw new RuntimeException("EpicBosses compatibility temporarily disabled");
		//return EntityFinder.get(CustomBosses.get().getBossEntityContainer().getData().get(name).getEntityStats().get(0).getMainStats().getEntityType().split(":")[0]).getEntityType();
	}
	
	/*@EventHandler
	public void onBossDeath(BossDeathEvent e){
		LivingEntity en = e.getActiveBossHolder().getLivingEntity();
		if (en.getKiller() == null) return;
		Bukkit.getPluginManager().callEvent(new CompatMobDeathEvent(e.getActiveBossHolder().getName(), en.getKiller(), en));
	}*/
	
}