package fr.skytasul.quests.utils.compatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.skytasul.quests.utils.MissingDependencyException;

public class WorldGuard {

	private static WorldGuardPlugin plugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	private static Method region;
	
	private static Method get;
	private static Method adapt;
	private static Object container;
	
	static {
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
			}catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
				ex.printStackTrace();
			}
		}catch (ClassNotFoundException e1) {
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
		return getRegionManager(w).getRegion(name);
	}
	
}
