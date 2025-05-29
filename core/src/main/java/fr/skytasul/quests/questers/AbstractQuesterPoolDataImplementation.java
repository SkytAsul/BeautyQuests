package fr.skytasul.quests.questers;

import fr.skytasul.quests.BeautyQuests;
import fr.skytasul.quests.api.questers.QuesterPoolData;
import fr.skytasul.quests.structure.pools.QuestPoolImplementation;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractQuesterPoolDataImplementation implements QuesterPoolData {

	protected final int poolId;

	protected long lastGive;
	protected Set<Integer> completedQuests = new HashSet<>();

	public AbstractQuesterPoolDataImplementation(int poolId) {
		this.poolId = poolId;
	}

	@Override
	public int getPoolID() {
		return poolId;
	}

	@Override
	public QuestPoolImplementation getPool() {
		return BeautyQuests.getInstance().getPoolsManager().getPool(poolId);
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
	}

}
