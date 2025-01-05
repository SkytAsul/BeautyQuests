package fr.skytasul.quests.players;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.structure.pools.QuestPoolImplementation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerPoolDatasImplementation implements QuesterPoolData {
	
	protected final PlayerQuesterImplementation acc;
	protected final int poolID;
	
	private long lastGive;
	private Set<Integer> completedQuests;
	
	public PlayerPoolDatasImplementation(PlayerQuesterImplementation acc, int poolID) {
		this(acc, poolID, 0, new HashSet<>());
	}
	
	public PlayerPoolDatasImplementation(PlayerQuesterImplementation acc, int poolID, long lastGive, Set<Integer> completedQuests) {
		this.acc = acc;
		this.poolID = poolID;
		this.lastGive = lastGive;
		this.completedQuests = completedQuests;
	}
	
	@Override
	public PlayerQuesterImplementation getQuester() {
		return acc;
	}
	
	@Override
	public int getPoolID() {
		return poolID;
	}
	
	@Override
	public QuestPoolImplementation getPool() {
		return BeautyQuests.getInstance().getPoolsManager().getPool(poolID);
	}
	
	@Override
	public long getLastGive() {
		return lastGive;
	}
	
	@Override
	public void setLastGive(long lastGive) {
		this.lastGive = lastGive;
	}
	
	@Override
	public Set<Integer> getCompletedQuests() {
		return completedQuests;
	}
	
	@Override
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
	
	public static PlayerPoolDatasImplementation deserialize(PlayerQuesterImplementation acc, Map<String, Object> map) {
		PlayerPoolDatasImplementation datas = new PlayerPoolDatasImplementation(acc, (int) map.get("poolID"));
		datas.lastGive = Utils.parseLong(map.get("lastGive"));
		datas.completedQuests = (Set<Integer>) map.get("completedQuests");
		return datas;
	}
	
}
