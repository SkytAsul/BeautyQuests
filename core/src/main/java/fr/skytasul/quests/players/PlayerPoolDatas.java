package fr.skytasul.quests.players;

import java.util.ArrayList;
import java.util.List;

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
	
}
