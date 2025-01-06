package fr.skytasul.quests.questers;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.questers.Quester;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.api.utils.Utils;
import fr.skytasul.quests.structure.pools.QuestPoolImplementation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuesterPoolDataImplementation implements QuesterPoolData {

	protected final Quester quester;
	protected final int poolID;

	private long lastGive;
	private Set<Integer> completedQuests;

	public QuesterPoolDataImplementation(Quester quester, int poolID) {
		this(quester, poolID, 0, new HashSet<>());
	}

	public QuesterPoolDataImplementation(Quester quester, int poolID, long lastGive, Set<Integer> completedQuests) {
		this.quester = quester;
		this.poolID = poolID;
		this.lastGive = lastGive;
		this.completedQuests = completedQuests;
	}

	@Override
	public Quester getQuester() {
		return quester;
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

	public static QuesterPoolDataImplementation deserialize(Quester quester, Map<String, Object> map) {
		QuesterPoolDataImplementation datas = new QuesterPoolDataImplementation(quester, (int) map.get("poolID"));
		datas.lastGive = Utils.parseLong(map.get("lastGive"));
		datas.completedQuests = (Set<Integer>) map.get("completedQuests");
		return datas;
	}

}
