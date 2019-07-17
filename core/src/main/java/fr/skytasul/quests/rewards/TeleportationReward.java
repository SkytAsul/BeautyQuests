package fr.skytasul.quests.rewards;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.skytasul.quests.api.rewards.AbstractReward;

public class TeleportationReward extends AbstractReward {

	public Location teleportation;

	public TeleportationReward(){
		super("tpReward");
	}
	
	public TeleportationReward(Location teleportation){
		this();
		this.teleportation = teleportation;
	}

	public String give(Player p){
		p.teleport(teleportation);
		return null;
	}

	
	protected void save(Map<String, Object> datas){
		datas.put("tp", teleportation.serialize());
	}

	protected void load(Map<String, Object> savedDatas){
		teleportation = Location.deserialize((Map<String, Object>) savedDatas.get("tp"));
	}

}
