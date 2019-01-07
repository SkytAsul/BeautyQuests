package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.utils.Lang;

public class StageInteract extends AbstractStage {

	private Location lc;
	private boolean left;
	
	public StageInteract(StageManager manager, Location location, boolean leftClick){
		super(manager);
		this.lc = location.getBlock().getLocation();
		this.left = leftClick;
	}

	public Location getLocation(){
		return lc;
	}

	public boolean needLeftClick(){
		return left;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e){
		if (e.getClickedBlock() == null) return;
		if (left){
			if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;
		}else if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!e.getClickedBlock().getLocation().equals(lc)) return;
		if (manager.hasStageLaunched(PlayersManager.getPlayerAccount(e.getPlayer()), getThis())){
			if (left) e.setCancelled(true);
			finishStage(e.getPlayer());
		}
	}
	
	protected String descriptionLine(PlayerAccount acc){
		return Lang.SCOREBOARD_INTERACT.format(lc.getBlockX() + " " + lc.getBlockY() + " " + lc.getBlockZ());
	}

	protected Map<String, Object> serialize(Map<String, Object> map){
		map.put("leftClick", left);
		map.put("location", lc.serialize());
		return map;
	}
	
	public static AbstractStage deserialize(Map<String, Object> map, StageManager manager){
		return new StageInteract(manager, Location.deserialize((Map<String, Object>) map.get("location")), (boolean) map.get("leftClick"));
	}


}
