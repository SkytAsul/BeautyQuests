package fr.skytasul.quests.players;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.skytasul.quests.utils.Utils;

public class PlayerPoolDatas {
	
	private final int poolID;
	
	private long lastGive = 0;
	private List<Integer> completedQuests = new ArrayList<>();
	
	public PlayerPoolDatas(int poolID) {
		this.poolID = poolID;
	}
	
	public int getPoolID() {
		return poolID;
	}
	
	public long getLastGive() {
		return lastGive;
	}
	
	public void setLastGive(long lastGive) {
		this.lastGive = lastGive;
	}
	
	public List<Integer> getCompletedQuests() {
		return completedQuests;
	}
	
	public void setCompletedQuests(List<Integer> completedQuests) {
		this.completedQuests = completedQuests;
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("poolID", poolID);
		map.put("lastGive", lastGive);
		map.put("completedQuests", completedQuests);
		
		return map;
	}
	
	public static PlayerPoolDatas deserialize(Map<String, Object> map) {
		PlayerPoolDatas datas = new PlayerPoolDatas((int) map.get("poolID"));
		datas.lastGive = Utils.parseLong(map.get("lastGive"));
		datas.completedQuests = (List<Integer>) map.get("completedQuests");
		return datas;
	}
	
}
