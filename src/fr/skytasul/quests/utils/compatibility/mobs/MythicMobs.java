package fr.skytasul.quests.utils.compatibility.mobs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;

public class MythicMobs implements Listener{

	public static String getInternalName(Object mob){
		return ((MythicMob) mob).getInternalName();
	}

	public static String getDisplayName(Object mob){
		return ((MythicMob) mob).getDisplayName();
	}
	
	public static MythicMob getMythicMob(String name){
		return io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMythicMob(name);
	}
	
	public static boolean isMythicMob(Entity en){
		return io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().isActiveMob(en.getUniqueId());
	}
	
	public static void sendMythicMobsList(Player p){
		Utils.sendMessage(p, Lang.MYTHICMOB_LIST.toString());
		StringBuilder stb = new StringBuilder("§a");
		for (MythicMob mm : io.lumine.xikage.mythicmobs.MythicMobs.inst().getMobManager().getMobTypes()){
			stb.append(mm.getInternalName() + "; ");
		}
		Utils.sendMessage(p, stb.toString());
	}
	
	@EventHandler
	public void onMythicDeath(io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent e){
		if (e.getKiller() == null) return;
		if (!(e.getKiller() instanceof Player)) return;
		Bukkit.getPluginManager().callEvent(new CompatMobDeathEvent(e.getMob().getType(), (Player) e.getKiller(), e.getEntity()));
	}
	
}
