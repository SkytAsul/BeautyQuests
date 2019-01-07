package fr.skytasul.quests.stages;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.compatibility.WorldGuard;

public class StageArea extends AbstractStage{
	
	private ProtectedRegion region;
	private World world;
	
	public StageArea(StageManager manager, String regionName, String worldName){
		super(manager);
		
		World w = Bukkit.getWorld(worldName);
		Validate.notNull(w, "No world with specified name (\"" + worldName + "\")");
		this.world = w;
		
		ProtectedRegion region = WorldGuard.getRegion(regionName, w);
		Validate.notNull(w, "No region with specified name (\"" + regionName + "\")");
		this.region = region;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return;
		if (manager.hasStageLaunched(PlayersManager.getPlayerAccount(e.getPlayer()), getThis())){
			if (WorldGuard.isInRegion(region, e.getTo())){
				finishStage(e.getPlayer());
			}
		}
	}

	public String descriptionLine(PlayerAccount acc){
		return Utils.format(Lang.SCOREBOARD_REG.toString(), region.getId());
	}
	
	public ProtectedRegion getRegion(){
		return region;
	}
	
	public World getWorld(){
		return world;
	}
	

	
	public Map<String, Object> serialize(Map<String, Object> map){
		map.put("region", region.getId());
		map.put("world", world.getName());
		return map;
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		AbstractStage st = new StageArea(manager, (String) map.get("region"), (String) map.get("world"));
		return st;
	}

}
