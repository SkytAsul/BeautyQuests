package fr.skytasul.quests.players;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.structure.pools.QuestPool;
import fr.skytasul.quests.utils.Utils;

public class PlayerPoolDatas {
	
	protected final PlayerAccount acc;
	protected final int poolID;
	
	private long lastGive;
	private Set<Integer> completedQuests;
	
	public PlayerPoolDatas(PlayerAccount acc, int poolID) {
		this(acc, poolID, 0, new HashSet<>());
	}
	
	public PlayerPoolDatas(PlayerAccount acc, int poolID, long lastGive, Set<Integer> completedQuests) {
		this.acc = acc;
		this.poolID = poolID;
		this.lastGive = lastGive;
		this.completedQuests = completedQuests;
	}
	
	public PlayerAccount getAccount() {
		return acc;
	}
	
	public int getPoolID() {
		return poolID;
	}
	
	public QuestPool getPool() {
		return BeautyQuests.getInstance().getPoolsManager().getPool(poolID);
	}
	
	public long getLastGive() {
		return lastGive;
	}
	
	public void setLastGive(long lastGive) {
		this.lastGive = lastGive;
	}
	
	public Set<Integer> getCompletedQuests() {
		return completedQuests;
	}
	
	public void setCompletedQuests(Set<Integer> completedQuests) {
		this.completedQuests = completedQuests;
		updatedCompletedQuests();
	}
	
	public void updatedCompletedQuests() {}
	
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("poolID", poolID);
		map.put("lastGive", lastGive);
		map.put("completedQuests", completedQuests);
		
		return map;
	}
	
	public static PlayerPoolDatas deserialize(PlayerAccount acc, Map<String, Object> map) {
		PlayerPoolDatas datas = new PlayerPoolDatas(acc, (int) map.get("poolID"));
		datas.lastGive = Utils.parseLong(map.get("lastGive"));
		datas.completedQuests = (Set<Integer>) map.get("completedQuests");
		return datas;
	}
	
}
