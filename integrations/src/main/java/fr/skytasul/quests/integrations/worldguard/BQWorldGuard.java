package fr.skytasul.quests.integrations.worldguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import com.cryptomorin.xseries.XMaterial;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.SessionManager;
import fr.skytasul.quests.api.QuestsAPI;
import fr.skytasul.quests.api.QuestsPlugin;
import fr.skytasul.quests.api.gui.ItemUtils;
import fr.skytasul.quests.api.localization.Lang;
import fr.skytasul.quests.api.requirements.RequirementCreator;
import fr.skytasul.quests.api.stages.StageType;
import fr.skytasul.quests.api.utils.MissingDependencyException;

public class BQWorldGuard {

	private static BQWorldGuard instance;
	
	private WorldGuardPlugin plugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	private Method region;
	
	private Method get;
	private Method adapt;
	private Object container;
	
	private boolean handleEntry = false;
	
	protected BQWorldGuard() {
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
				SessionManager sessionManager = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getSessionManager();
				if (WorldGuardEntryHandler.FACTORY.register(sessionManager)) {
					handleEntry = true;
					QuestsPlugin.getPlugin().getLoggerExpanded().debug("Now using WorldGuard entry API.");
					WorldGuardEntryHandler.FACTORY.registerSessions(sessionManager);
					
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
	
	private void disable() {
		if (handleEntry) {
			handleEntry = false;
			WorldGuardEntryHandler.FACTORY.unregister(com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getSessionManager());
			QuestsPlugin.getPlugin().getLoggerExpanded().debug("Unregistered from WorldGuard entry API.");
		}
	}
	
	public boolean doHandleEntry() {
		return handleEntry;
	}
	
	public RegionManager getRegionManager(World world) {
		try {
			if (region != null) return (RegionManager) region.invoke(plugin, world);
			return (RegionManager) get.invoke(container, adapt.invoke(null, world));
		}catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getRegionID(Object region) {
		return ((ProtectedRegion) region).getId();
	}
	
	public World getWorld(String id) {
		for (World w : Bukkit.getWorlds()){
			try{
				if (getRegionManager(w).hasRegion(id)) return w;
			}catch (Exception ex){
				continue;
			}
		}
		return null;
	}

	public boolean isInRegion(ProtectedRegion region, Location to) {
		return region.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ());
	}
	
	public boolean regionExists(String name, World w) {
		return getRegionManager(w).getRegion(name) != null;
	}
	
	public ProtectedRegion getRegion(String name, World w) {
		if (w == null) return null;
		return getRegionManager(w).getRegion(name);
	}
	
	public static void initialize() {
		Validate.isTrue(instance == null, "BQ WorldGuard integration already initialized.");
		instance = new BQWorldGuard();
		
		QuestsAPI.getAPI().getStages().register(new StageType<>("REGION", StageArea.class, Lang.Find.name(), StageArea::deserialize,
				ItemUtils.item(XMaterial.WOODEN_AXE, Lang.stageGoTo.toString()), StageArea.Creator::new));
		QuestsAPI.getAPI().getRequirements().register(new RequirementCreator("regionRequired", RegionRequirement.class, ItemUtils.item(XMaterial.WOODEN_AXE, Lang.RRegion.toString()), RegionRequirement::new));
	}
	
	public static void unload() {
		instance.disable();
	}

	public static BQWorldGuard getInstance() {
		return instance;
	}
	
}
