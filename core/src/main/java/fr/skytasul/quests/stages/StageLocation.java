package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;

public class StageLocation extends AbstractStage {

	private Location lc;
	private int radius;
	
	private String descMessage;
	
	public StageLocation(QuestBranch branch, Location lc, int radius){
		super(branch);
		this.lc = lc;
		this.radius = radius;
		
		this.descMessage = Lang.SCOREBOARD_LOCATION.format(lc.getBlockX(), lc.getBlockY(), lc.getBlockZ(), lc.getWorld().getName());
	}
	
	public Location getLocation(){
		return lc;
	}
	
	public int getRadius(){
		return radius;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e){
		if (e.getTo().getWorld() != lc.getWorld()) return;
		if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockY() == e.getTo().getBlockY() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) return; // only rotation
		if (hasStarted(e.getPlayer())){
			if (e.getTo().distance(lc) <= radius) finishStage(e.getPlayer());
		}
	}
	
	protected String descriptionLine(PlayerAccount acc, Source source){
		return descMessage;
	}

	protected void serialize(Map<String, Object> map){
		map.put("location", lc.serialize());
		map.put("radius", radius);
	}

	public static AbstractStage deserialize(Map<String, Object> map, QuestBranch branch){
		return new StageLocation(branch, Location.deserialize((Map<String, Object>) map.get("location")), (int) map.get("radius"));
	}
	
}
