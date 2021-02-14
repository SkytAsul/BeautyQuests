package fr.skytasul.quests.utils.compatibility.worldguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler.Factory;

import fr.skytasul.quests.utils.DebugUtils;
import fr.skytasul.quests.utils.compatibility.MissingDependencyException;

public class BQWorldGuard {

	private static WorldGuardPlugin plugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	private static Method region;
	
	private static Method get;
	private static Method adapt;
	private static Object container;
	
	public static boolean handleEntry = false;
	
	public static void init() {
		if (plugin == null) throw new MissingDependencyException("WorldGuard");
		try {
			Class<?> wg = Class.forName("com.sk89q.worldguard.WorldGuard");
			try {
				container =
						Class.forName("com.sk89q.worldguard.internal.platform.WorldGuardPlatform").getDeclaredMethod("getRegionContainer").invoke(
						Class.forName("com.sk89q.worldguard.WorldGuard").getDeclaredMethod("getPlatform").invoke(
						wg.getDeclaredMethod("getInstance").invoke(null)));
				get = Class.forName("com.sk89q.worldguard.protection.regions.RegionContainer").getDeclaredMethod("get", com.sk89q.worldedit.world.World.class);
				adapt = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter").getDeclaredMethod("adapt", World.class);
				if (com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getSessionManager().registerHandler(new Factory<WorldGuardEntryHandler>() {
					@Override
					public WorldGuardEntryHandler create(Session session) {
						return new WorldGuardEntryHandler(session);
					}
				}, null)) {
					handleEntry = true;
					DebugUtils.logMessage("Now using WorldGuard entry API.");
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}catch (ClassNotFoundException e1) { // WorldGuard 6 and below
			try {
				region = plugin.getClass().getDeclaredMethod("getRegionManager", World.class);
			}catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static RegionManager getRegionManager(World world){
		try {
			if (region != null) return (RegionManager) region.invoke(plugin, world);
			return (RegionManager) get.invoke(container, adapt.invoke(null, world));
			//return com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getRegionID(Object region){
		return ((ProtectedRegion) region).getId();
	}
	
	public static World getWorld(String id){
		for (World w : Bukkit.getWorlds()){
			try{
				if (getRegionManager(w).hasRegion(id)) return w;
			}catch (Exception ex){
				continue;
			}
		}
		return null;
	}

	public static boolean isInRegion(ProtectedRegion region, Location to){
		return region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());
		/*for(ProtectedRegion rg : getRegionManager(to.getWorld()).getApplicableRegions(to)){
			if (region.getId().equals(rg.getId())) return true;
		}
		return false;*/
	}
	
	public static boolean regionExists(String name, World w){
		return getRegionManager(w).getRegion(name) != null;
	}
	
	public static ProtectedRegion getRegion(String name, World w){
		if (w == null) return null;
		return getRegionManager(w).getRegion(name);
	}
	
}
